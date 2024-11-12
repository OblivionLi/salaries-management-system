package com.balaur.backend.controllers;

import com.balaur.backend.requests.SalaryRequest;
import com.balaur.backend.responses.SalariesResponseWrapper;
import com.balaur.backend.responses.SalaryResponse;
import com.balaur.backend.services.SalaryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/salaries")
@RequiredArgsConstructor
public class SalaryController {
    private final SalaryService mainService;

    @GetMapping("/")
    public ResponseEntity<SalariesResponseWrapper> getSalaries() {
        return mainService.getSalaries();
    }

    @PostMapping("/add")
    public ResponseEntity<SalaryResponse> addSalary(@Valid @RequestBody SalaryRequest salaryRequest) {
        return mainService.addSalary(salaryRequest);
    }

    @PatchMapping("/edit/{id}")
    public ResponseEntity<SalaryResponse> editSalary(@PathVariable Long id, @Valid @RequestBody SalaryRequest salaryRequest) {
        return mainService.editSalary(id, salaryRequest);
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<SalariesResponseWrapper> deleteSalary(@PathVariable Long id) {
        return mainService.deleteSalary(id);
    }
}
