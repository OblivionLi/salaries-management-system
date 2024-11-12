package com.balaur.backend.responses;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Builder
@Getter
public class SalaryResponse {
    private Long salaryId;
    private BigDecimal salary;
    private String employee;
    private LocalDateTime salaryDate;
    private String message;
    private List<Link> links;
}
