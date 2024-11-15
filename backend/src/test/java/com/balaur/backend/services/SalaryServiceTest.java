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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SalaryServiceTest {

    @Mock
    private SalaryRepository salaryRepository;

    @Mock
    private SalaryKafkaProducer salaryKafkaProducer;

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private ValueOperations<String, Object> valueOperations;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private SalaryService salaryService;

    private Salary testSalary;
    private SalaryRequest testSalaryRequest;
    private final String SALARY_CACHE_KEY = "salaries";
    private final String version = "v1";

    @BeforeEach
    void setUp() {
        testSalary = new Salary();
        testSalary.setId(1L);
        testSalary.setSalary(BigDecimal.valueOf(5000));
        testSalary.setSalaryDate(LocalDateTime.now());
        testSalary.setEmployee("John Doe");

        testSalaryRequest = new SalaryRequest();
        testSalaryRequest.setSalary(BigDecimal.valueOf(5000));
        testSalaryRequest.setSalaryDate(LocalDateTime.now());
        testSalaryRequest.setEmployee("John Doe");

        lenient().when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    }

    @Test
    void getSalaries_WhenSalariesExistInCache_ReturnsOkResponse() {
        List<Salary> salaryList = List.of(testSalary);
        List<SalaryResponse> salaryResponses = salaryList.stream()
                .map(salary -> SalaryResponse.builder()
                        .salaryId(salary.getId())
                        .salary(salary.getSalary())
                        .salaryDate(salary.getSalaryDate())
                        .employee(salary.getEmployee())
                        .message(null)
                        .links(LinkUtils.generateLinks("get", version, String.valueOf(salary.getId())))
                        .build())
                .toList();

        SalariesResponseWrapper cachedWrapper = new SalariesResponseWrapper(salaryResponses, List.of(new Link("self", "/api/" + version + "/salaries", "GET", version)));

        when(valueOperations.get(SALARY_CACHE_KEY)).thenReturn(cachedWrapper);
        when(objectMapper.convertValue(any(), eq(SalariesResponseWrapper.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        ResponseEntity<SalariesResponseWrapper> response = salaryService.getSalaries();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().getData().size());
        assertEquals(testSalary.getId(), response.getBody().getData().getFirst().getSalaryId());

        verify(valueOperations).get(SALARY_CACHE_KEY);
        verify(salaryRepository, never()).findAll();
        verify(redisTemplate, times(1)).opsForValue();
    }

    @Test
    void getSalaries_WhenSalariesExistInDatabase_ReturnsOkResponse() {
        List<Salary> salaryList = List.of(testSalary);
        when(valueOperations.get(SALARY_CACHE_KEY)).thenReturn(null);
        when(salaryRepository.findAll()).thenReturn(salaryList);

        ResponseEntity<SalariesResponseWrapper> response = salaryService.getSalaries();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().getData().size());
        assertEquals(testSalary.getId(), response.getBody().getData().getFirst().getSalaryId());
        verify(salaryRepository).findAll();
    }

    @Test
    void getSalaries_WhenNoSalariesExist_ReturnsNotFoundResponse() {
        when(valueOperations.get(SALARY_CACHE_KEY)).thenReturn(null);
        when(salaryRepository.findAll()).thenReturn(new ArrayList<>());

        ResponseEntity<SalariesResponseWrapper> response = salaryService.getSalaries();

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().getData().size());
        assertEquals("Couldn't find any salary. Add a salary first.",
                response.getBody().getData().getFirst().getMessage());
        verify(salaryRepository).findAll();
    }

    @Test
    void addSalary_WhenSuccessful_ReturnsCreatedResponse() {
        when(salaryRepository.save(any(Salary.class))).thenReturn(testSalary);
        doNothing().when(salaryKafkaProducer).sendSalaryMessage(testSalary);

        ResponseEntity<SalaryResponse> response = salaryService.addSalary(testSalaryRequest);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(testSalary.getId(), response.getBody().getSalaryId());
        verify(salaryRepository).save(any(Salary.class));
    }

    @Test
    void addSalary_WhenSaveFails_ReturnsInternalServerError() {
        when(salaryRepository.save(any(Salary.class))).thenThrow(new RuntimeException());

        ResponseEntity<SalaryResponse> response = salaryService.addSalary(testSalaryRequest);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("An error occurred.", response.getBody().getMessage());
        verify(salaryRepository).save(any(Salary.class));
    }

    @Test
    void editSalary_WhenSalaryExists_ReturnsCreatedResponse() {
        when(salaryRepository.findById(1L)).thenReturn(Optional.of(testSalary));
        when(salaryRepository.save(any(Salary.class))).thenReturn(testSalary);
        doNothing().when(salaryKafkaProducer).sendSalaryMessage(testSalary);

        ResponseEntity<SalaryResponse> response = salaryService.editSalary(1L, testSalaryRequest);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(testSalary.getId(), response.getBody().getSalaryId());
        verify(salaryRepository).findById(1L);
        verify(salaryRepository).save(any(Salary.class));
    }

    @Test
    void editSalary_WhenSalaryNotFound_ReturnsNotFoundResponse() {
        when(salaryRepository.findById(1L)).thenReturn(Optional.empty());

        ResponseEntity<SalaryResponse> response = salaryService.editSalary(1L, testSalaryRequest);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("An error occurred.", response.getBody().getMessage());
        verify(salaryRepository).findById(1L);
        verify(salaryRepository, never()).save(any(Salary.class));
    }

    @Test
    void deleteSalary_WhenSalaryExists_ReturnsOkResponse() {
        when(salaryRepository.findById(1L)).thenReturn(Optional.of(testSalary));
        doNothing().when(salaryRepository).delete(any(Salary.class));

        ResponseEntity<SalariesResponseWrapper> response = salaryService.deleteSalary(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().getData().size());
        assertEquals(testSalary.getId(), response.getBody().getData().getFirst().getSalaryId());
        verify(salaryRepository).findById(1L);
        verify(salaryRepository).delete(any(Salary.class));
    }

    @Test
    void deleteSalary_WhenSalaryNotFound_ReturnsNotFoundResponse() {
        when(salaryRepository.findById(1L)).thenReturn(Optional.empty());

        ResponseEntity<SalariesResponseWrapper> response = salaryService.deleteSalary(1L);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("An error occurred.", response.getBody().getData().getFirst().getMessage());
        verify(salaryRepository).findById(1L);
        verify(salaryRepository, never()).delete(any(Salary.class));
    }

    @Test
    void deleteSalary_WhenDeleteFails_ReturnsInternalServerError() {
        when(salaryRepository.findById(1L)).thenReturn(Optional.of(testSalary));
        doThrow(new RuntimeException()).when(salaryRepository).delete(any(Salary.class));

        ResponseEntity<SalariesResponseWrapper> response = salaryService.deleteSalary(1L);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("An error occurred.", response.getBody().getData().getFirst().getMessage());
        verify(salaryRepository).findById(1L);
        verify(salaryRepository).delete(any(Salary.class));
    }
}