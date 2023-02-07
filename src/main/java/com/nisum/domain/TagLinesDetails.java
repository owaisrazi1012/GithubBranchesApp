package com.nisum.domain;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class TagLinesDetails {
    private String tagName;
    private String filePath;
    private Integer lineNumber;
}
