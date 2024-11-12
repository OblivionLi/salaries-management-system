package com.balaur.backend.services;

import com.balaur.backend.models.Salary;
import com.balaur.backend.repositories.SalaryRepository;
import com.balaur.backend.requests.SalaryRequest;
import com.balaur.backend.responses.Link;
import com.balaur.backend.responses.LinkUtils;
import com.balaur.backend.responses.SalariesResponseWrapper;
import com.balaur.backend.responses.SalaryResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class SalaryService {
    // TODO:: Add pagination (page, size, totalElements etc..) to "getSalaries" response

    private final SalaryRepository salaryRepository;
    private final String version = "v1";

    public ResponseEntity<SalariesResponseWrapper> getSalaries() {
        List<Salary> salariesList = salaryRepository.findAll();
        List<SalaryResponse> salariesResponses = new ArrayList<>();
        List<Link> mainLinks = List.of(new Link("self", "/api/" + version + "/salaries", "GET", version));

        if (salariesList.isEmpty()) {
            salariesResponses.add(SalaryResponse.builder()
                    .salaryId(-1L)
                    .salary(BigDecimal.valueOf(0))
                    .salaryDate(null)
                    .employee("-")
                    .message("Couldn't find any salary. Add a salary first.")
                    .links(List.of(new Link("self", "/api/" + version + "/salaries", "POST", version)))
                    .build()
            );

            log.warn("[SalaryService.getSalaries] Couldn't find any salary.");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                    new SalariesResponseWrapper(salariesResponses, mainLinks)
            );
        }

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

        log.info("[SalaryService.getSalaries] Salaries found.");
        return ResponseEntity.status(HttpStatus.OK).body(new SalariesResponseWrapper(salariesResponses, mainLinks));
    }

    public ResponseEntity<SalaryResponse> addSalary(SalaryRequest salaryRequest) {
        Salary newSalary = new Salary();
        return getSalaryResponseResponseEntity(salaryRequest, newSalary);
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
        return getSalaryResponseResponseEntity(salaryRequest, salaryToEdit);
    }

    private ResponseEntity<SalaryResponse> getSalaryResponseResponseEntity(@Valid SalaryRequest salaryRequest, Salary salaryToEdit) {
        salaryToEdit.setSalary(salaryRequest.getSalary());
        salaryToEdit.setSalaryDate(salaryRequest.getSalaryDate());
        salaryToEdit.setEmployee(salaryRequest.getEmployee());

        try {
            log.info("[SalaryService.getSalaryResponseResponseEntity] Trying to save salary.");
            Salary savedSalary = salaryRepository.save(salaryToEdit);
            return ResponseEntity.status(HttpStatus.CREATED).body(buildSalaryResponse(savedSalary, "add"));
        } catch (Exception e) {
            log.error("[SalaryService.getSalaryResponseResponseEntity] Something happened while trying to save salary");
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
}
