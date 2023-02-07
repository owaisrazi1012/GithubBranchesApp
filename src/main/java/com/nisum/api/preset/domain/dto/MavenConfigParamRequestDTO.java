package com.nisum.api.preset.domain.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MavenConfigParamRequestDTO {
    private Long id;
    private String key;
    private String value;

    @JsonIgnore
    public MavenConfigParamRequestDTO getMavenConfigParamRequestDTO(){
        return this;
    }
}
