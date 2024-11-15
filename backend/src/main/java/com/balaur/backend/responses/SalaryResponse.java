package com.balaur.backend.responses;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Builder
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class SalaryResponse {
    private Long salaryId;
    private BigDecimal salary;
    private String employee;
    private LocalDateTime salaryDate;
    private String message;
    private List<Link> links;
}
