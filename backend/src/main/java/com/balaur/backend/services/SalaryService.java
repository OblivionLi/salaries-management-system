package com.balaur.backend.services;

import com.balaur.backend.kafka.SalaryKafkaProducer;
import com.balaur.backend.models.Salary;
import com.balaur.backend.repositories.SalaryRepository;
import com.balaur.backend.requests.SalaryRequest;
import com.balaur.backend.responses.Link;
import com.balaur.backend.responses.LinkUtils;
import com.balaur.backend.responses.SalariesResponseWrapper;
import com.balaur.backend.responses.SalaryResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class SalaryService {
    // TODO:: Add pagination (page, size, totalElements etc..) to "getSalaries" response

    private final SalaryRepository salaryRepository;
    private final String version = "v1";
    private final SalaryKafkaProducer salaryKafkaProducer;
    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;

    private final String SALARY_CACHE_KEY = "salaries";
    private static final Duration CACHE_TTL = Duration.ofMinutes(30);

    public ResponseEntity<SalariesResponseWrapper> getSalaries() {
        SalariesResponseWrapper salariesResponseWrapper = getSalariesResponseFromRedisCache();
        if (salariesResponseWrapper != null) {
            return ResponseEntity.status(HttpStatus.OK).body(salariesResponseWrapper);
        }

        List<Link> mainLinks = List.of(new Link("self", "/api/" + version + "/salaries", "GET", version));
        salariesResponseWrapper = getSalariesResponseFromDB(mainLinks);
        if (salariesResponseWrapper != null) {
            saveSalariesResponseToRedisCache(salariesResponseWrapper);
            return ResponseEntity.status(HttpStatus.OK).body(salariesResponseWrapper);
        }

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(getSalariesResponseForEmptyList(mainLinks));
    }

    private SalariesResponseWrapper getSalariesResponseForEmptyList(List<Link> mainLinks) {
        List<SalaryResponse> salariesResponses = new ArrayList<>();
        salariesResponses.add(SalaryResponse.builder()
                .salaryId(-1L)
                .salary(BigDecimal.valueOf(0))
                .salaryDate(null)
                .employee("-")
                .message("Couldn't find any salary. Add a salary first.")
                .links(List.of(new Link("self", "/api/" + version + "/salaries", "POST", version)))
                .build()
        );

        log.warn("[SalaryService.getSalariesResponseForEmptyList] Couldn't find any salary.");
        return new SalariesResponseWrapper(salariesResponses, mainLinks);
    }

    private void saveSalariesResponseToRedisCache(SalariesResponseWrapper salariesResponseWrapper) {
        log.info("[SalaryService.saveSalariesResponseToRedisCache] Saving response to redis cache: {}", salariesResponseWrapper.toString());
        redisTemplate.opsForValue().set(SALARY_CACHE_KEY, salariesResponseWrapper, CACHE_TTL);
    }

    private SalariesResponseWrapper getSalariesResponseFromRedisCache() {
        log.info("[SalaryService.getSalariesResponseFromRedisCache] Attempting to retrieve data from redis cache.");

        if (redisTemplate.hasKey(SALARY_CACHE_KEY) == null) {
            log.info("[SalaryService.getSalariesResponseFromRedisCache] No data found in redis cache.");
            return null;
        }

        try {
            Object cachedValue = redisTemplate.opsForValue().get(SALARY_CACHE_KEY);

            if (cachedValue == null) {
                log.info("[SalaryService.getSalariesResponseFromRedisCache] No data found in redis cache.");
                return null;
            }

            log.info("[SalaryService.getSalariesResponseFromRedisCache] Data found in redis cache. Beginning deserialization. {}", cachedValue);
            return objectMapper.convertValue(cachedValue, SalariesResponseWrapper.class);
        } catch (Exception e) {
            log.error("[SalaryService.getSalariesResponseFromRedisCache] Error deserializing cache data: ", e);
            return null;
        }
    }

    private SalariesResponseWrapper getSalariesResponseFromDB(List<Link> mainLinks) {
        List<Salary> salariesList;

        try {
            salariesList = salaryRepository.findAll();
        } catch (Exception e) {
            log.error("[SalaryService.getSalariesResponseFromDB] Error retrieving salaries: {}", e.getMessage());
            throw new RuntimeException("Error retrieving salaries", e);
        }

        if (salariesList.isEmpty()) {
            log.info("[SalaryService.getSalariesResponseFromDB] No data found in database.");
            return null;
        }

        log.info("[SalaryService.getSalariesResponseFromDB] Data found in database. Beginning building the response.");
        return buildSalariesResponseWrapper(salariesList, mainLinks);
    }

    private SalariesResponseWrapper buildSalariesResponseWrapper(List<Salary> salariesList, List<Link> mainLinks) {
        List<SalaryResponse> salariesResponses = new ArrayList<>();

        for (Salary salary : salariesList) {
            salariesResponses.add(
                    SalaryResponse.builder()
                            .salaryId(salary.getId())
                            .salary(salary.getSalary())
                            .salaryDate(salary.getSalaryDate())
                            .employee(salary.getEmployee())
                            .message(null)
                            .links(LinkUtils.generateLinks("get", version, String.valueOf(salary.getId())))
                            .build()
            );
        }

        return new SalariesResponseWrapper(salariesResponses, mainLinks);
    }

    public ResponseEntity<SalaryResponse> addSalary(SalaryRequest salaryRequest) {
        Salary newSalary = new Salary();
        return getSalaryResponseEntity(salaryRequest, newSalary);
    }

    private SalaryResponse buildSalaryResponse(Salary salary, String method) {
        log.info("[SalaryService.buildSalaryResponse] Salary object built.");
        return SalaryResponse.builder()
                .salaryId(salary.getId())
                .salary(salary.getSalary())
                .salaryDate(salary.getSalaryDate())
                .employee(salary.getEmployee())
                .message(null)
                .links(LinkUtils.generateLinks(method, version, String.valueOf(salary.getId())))
                .build();
    }

    private SalaryResponse buildSalaryErrorResponse(Long id, String rel, String method) {
        log.warn("[SalaryService.buildSalaryResponse] Salary is null.");
        return SalaryResponse.builder()
                .salaryId(-1L)
                .salary(BigDecimal.valueOf(0))
                .salaryDate(null)
                .employee("-")
                .message("An error occurred.")
                .links(LinkUtils.generateErrorLink(rel, method, version, String.valueOf(id)))
                .build();
    }

    public ResponseEntity<SalaryResponse> editSalary(Long id, @Valid SalaryRequest salaryRequest) {
        Optional<Salary> salaryOptional;
        try {
            log.info("[SalaryService.editSalary] Trying to edit salary by id: {}.", id);
            salaryOptional = salaryRepository.findById(id);
        } catch (Exception e) {
            log.error("[SalaryService.editSalary] Something happened while trying to find salary by id: {}", id);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(buildSalaryErrorResponse(id, "edit", "PATCH"));
        }

        if (salaryOptional.isEmpty()) {
            log.warn("[SalaryService.editSalary] Could not find salary by id: {}", id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(buildSalaryErrorResponse(id, "edit", "PATCH"));
        }

        Salary salaryToEdit = salaryOptional.get();
        return getSalaryResponseEntity(salaryRequest, salaryToEdit);
    }

    public ResponseEntity<SalaryResponse> getSalaryResponseEntity(@Valid SalaryRequest salaryRequest, Salary salaryToEdit) {
        salaryToEdit.setSalary(salaryRequest.getSalary());
        salaryToEdit.setSalaryDate(salaryRequest.getSalaryDate());
        salaryToEdit.setEmployee(salaryRequest.getEmployee());

        try {
            log.info("[SalaryService.getSalaryResponseEntity] Trying to save salary.");
            Salary savedSalary = salaryRepository.save(salaryToEdit);

            salaryKafkaProducer.sendSalaryMessage(savedSalary);
            clearSalariesCache();
            return ResponseEntity.status(HttpStatus.CREATED).body(buildSalaryResponse(savedSalary, "add"));
        } catch (Exception e) {
            log.error("[SalaryService.getSalaryResponseEntity] Something happened while trying to save salary");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(buildSalaryErrorResponse(-1L, "add", "POST"));
        }
    }

    public ResponseEntity<SalariesResponseWrapper> deleteSalary(Long id) {
        Optional<Salary> salaryOptional;
        List<Link> mainLinks = List.of(new Link("self", "/salaries/delete/" + id, "DELETE", version));

        try {
            log.info("[SalaryService.deleteSalary] Trying to find salary by id: {}", id);
            salaryOptional = salaryRepository.findById(id);
        } catch (Exception e) {
            log.error("[SalaryService.deleteSalary] Something happened while trying to find salary: {}", id);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    new SalariesResponseWrapper(List.of(buildSalaryErrorResponse(id, "delete", "DELETE")), mainLinks)
            );
        }

        if (salaryOptional.isEmpty()) {
            log.warn("[SalaryService.deleteSalary] Could not find salary by id: {}", id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                    new SalariesResponseWrapper(List.of(buildSalaryErrorResponse(id, "delete", "DELETE")), mainLinks)
            );
        }

        SalaryResponse salaryResponse = buildSalaryResponse(salaryOptional.get(), "delete");
        try {
            log.info("[SalaryService.deleteSalary] Trying to delete salary by id: {}.", id);
            salaryRepository.delete(salaryOptional.get());

            clearSalariesCache();
            return ResponseEntity.status(HttpStatus.OK).body(
                    new SalariesResponseWrapper(List.of(salaryResponse), mainLinks)
            );
        } catch (Exception e) {
            log.error("[SalaryService.deleteSalary] Something happened while trying to delete salary by id: {}.", id);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    new SalariesResponseWrapper(List.of(buildSalaryErrorResponse(id, "delete", "DELETE")), mainLinks)
            );
        }
    }

    private void clearSalariesCache() {
        try {
            redisTemplate.delete(SALARY_CACHE_KEY);
            log.info("[SalaryService.clearSalariesCache] Successfully cleared salaries cache");
        } catch (Exception e) {
            log.error("[SalaryService.clearSalariesCache] Error clearing salaries cache: {}", e.getMessage());
        }
    }

    public void refreshSalariesCache() {
        try {
            List<Salary> salaries = salaryRepository.findAll();
            redisTemplate.opsForValue().set(SALARY_CACHE_KEY, salaries, CACHE_TTL);
            log.info("[SalaryService.refreshSalariesCache] Successfully refreshed salaries cache");
        } catch (Exception e) {
            log.error("[SalaryService.refreshSalariesCache] Error refreshing salaries cache: {}", e.getMessage());
        }
    }
}
