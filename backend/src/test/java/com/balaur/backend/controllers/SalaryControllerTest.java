package com.balaur.backend.controllers;

import com.balaur.backend.requests.SalaryRequest;
import com.balaur.backend.responses.Link;
import com.balaur.backend.responses.SalariesResponseWrapper;
import com.balaur.backend.responses.SalaryResponse;
import com.balaur.backend.services.SalaryService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(SalaryController.class)
class SalaryControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private SalaryService salaryService;

    private ObjectMapper objectMapper;
    private SalaryRequest validSalaryRequest;
    private SalaryResponse sampleSalaryResponse;
    private List<Link> sampleLinks;
    private final String version = "v1";


    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        // Setup sample Links
        sampleLinks = Collections.singletonList(
                Link.builder()
                        .rel("self")
                        .href("/api/v1/salaries/")
                        .method("GET")
                        .version(version)
                        .build()
        );

        // Setup valid salary request
        validSalaryRequest = new SalaryRequest();
        validSalaryRequest.setEmployee("John Doe");
        validSalaryRequest.setSalary(new BigDecimal("5000.00"));
        validSalaryRequest.setSalaryDate(LocalDateTime.now());

        // Setup sample salary response
        sampleSalaryResponse = SalaryResponse.builder()
                .salaryId(1L)
                .salary(new BigDecimal("5000.00"))
                .employee("John Doe")
                .salaryDate(LocalDateTime.now())
                .message("Salary created successfully")
                .links(sampleLinks)
                .build();
    }

    @Test
    @DisplayName("GET /api/v1/salaries/ - Success")
    void getSalaries_ReturnsAllSalaries() throws Exception {
        List<SalaryResponse> salaries = Collections.singletonList(sampleSalaryResponse);
        SalariesResponseWrapper wrapper = new SalariesResponseWrapper(salaries, sampleLinks);
        when(salaryService.getSalaries()).thenReturn(ResponseEntity.ok(wrapper));

        mockMvc.perform(get("/api/v1/salaries/"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.data[0].salaryId").value(1))
                .andExpect(jsonPath("$.data[0].employee").value("John Doe"))
                .andExpect(jsonPath("$.links[0].rel").value("self"));
    }

    @Test
    @DisplayName("POST /api/v1/salaries/add - Success")
    void addSalary_WithValidRequest_ReturnsSalaryResponse() throws Exception {
        when(salaryService.addSalary(any(SalaryRequest.class)))
                .thenReturn(ResponseEntity.ok(sampleSalaryResponse));

        mockMvc.perform(post("/api/v1/salaries/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validSalaryRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.salaryId").value(1))
                .andExpect(jsonPath("$.employee").value("John Doe"))
                .andExpect(jsonPath("$.message").value("Salary created successfully"));
    }

    @Test
    @DisplayName("POST /api/v1/salaries/add - Validation Error")
    void addSalary_WithInvalidRequest_ReturnsBadRequest() throws Exception {
        SalaryRequest invalidRequest = new SalaryRequest();
        invalidRequest.setEmployee(""); // Invalid empty employee name
        invalidRequest.setSalary(new BigDecimal("-1000")); // Invalid negative salary

        mockMvc.perform(post("/api/v1/salaries/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("PATCH /api/v1/salaries/edit/{id} - Success")
    void editSalary_WithValidRequest_ReturnsSalaryResponse() throws Exception {
        Long salaryId = 1L;
        when(salaryService.editSalary(eq(salaryId), any(SalaryRequest.class)))
                .thenReturn(ResponseEntity.ok(sampleSalaryResponse));

        mockMvc.perform(patch("/api/v1/salaries/edit/" + salaryId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validSalaryRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.salaryId").value(1))
                .andExpect(jsonPath("$.employee").value("John Doe"));
    }

    @Test
    @DisplayName("DELETE /api/v1/salaries/delete/{id} - Success")
    void deleteSalary_WithValidId_ReturnsUpdatedList() throws Exception {
        Long salaryId = 1L;
        List<SalaryResponse> updatedSalaries = List.of(); // Empty list after deletion
        SalariesResponseWrapper wrapper = new SalariesResponseWrapper(updatedSalaries, sampleLinks);
        when(salaryService.deleteSalary(salaryId)).thenReturn(ResponseEntity.ok(wrapper));

        mockMvc.perform(delete("/api/v1/salaries/delete/" + salaryId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data").isEmpty());
    }

    @Test
    @DisplayName("PATCH /api/v1/salaries/edit/{id} - Invalid ID Format")
    void editSalary_WithInvalidIdFormat_ReturnsBadRequest() throws Exception {
        mockMvc.perform(patch("/api/v1/salaries/edit/invalid-id")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validSalaryRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/v1/salaries/add - Null Request Body")
    void addSalary_WithNullRequestBody_ReturnsBadRequest() throws Exception {
        mockMvc.perform(post("/api/v1/salaries/add")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }
}