package com.balaur.backend.responses;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
public class Link {
    private String rel;
    private String href;
    private String method;
    private String version;
}
