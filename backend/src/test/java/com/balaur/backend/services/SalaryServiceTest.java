package com.balaur.backend.services;

import com.balaur.backend.kafka.SalaryKafkaProducer;
import com.balaur.backend.models.Salary;
import com.balaur.backend.repositories.SalaryRepository;
import com.balaur.backend.requests.SalaryRequest;
import com.balaur.backend.responses.SalariesResponseWrapper;
import com.balaur.backend.responses.SalaryResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
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

    @InjectMocks
    private SalaryService salaryService;

    private Salary testSalary;
    private SalaryRequest testSalaryRequest;

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
    }

    @Test
    void getSalaries_WhenSalariesExist_ReturnsOkResponse() {
        List<Salary> salaryList = List.of(testSalary);
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