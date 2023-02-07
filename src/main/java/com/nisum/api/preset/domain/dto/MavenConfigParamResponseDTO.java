package com.nisum.api.preset.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MavenConfigParamResponseDTO {
    private Long id;
    private String key;
    private String value;
}
