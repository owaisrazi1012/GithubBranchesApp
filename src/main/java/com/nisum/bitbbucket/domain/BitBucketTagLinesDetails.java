package com.nisum.bitbbucket.domain;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Set;

@Data
@AllArgsConstructor
public class BitBucketTagLinesDetails {
    private String tagName;
    private String filePath;
    private Set<Integer> lineNumbers;
}
