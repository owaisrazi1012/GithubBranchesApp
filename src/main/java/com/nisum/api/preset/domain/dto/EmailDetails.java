package com.nisum.api.preset.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EmailDetails {
    private String recipients;
    private String body;
    private String subject;
    private String attachment;
}