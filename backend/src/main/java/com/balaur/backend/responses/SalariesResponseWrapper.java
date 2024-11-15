package com.balaur.backend.responses;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class SalariesResponseWrapper {
    private List<SalaryResponse> data;
    private List<Link> links;
}
