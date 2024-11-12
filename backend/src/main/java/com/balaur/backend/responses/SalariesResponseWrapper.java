package com.balaur.backend.responses;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class SalariesResponseWrapper {
    private List<SalaryResponse> data;
    private List<Link> links;
}
