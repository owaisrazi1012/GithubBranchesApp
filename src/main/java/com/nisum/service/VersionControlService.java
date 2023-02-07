package com.nisum.service;

import com.nisum.domain.TagLinesDetails;

import java.util.List;
import java.util.Set;

public interface VersionControlService {
    Set<String> prepareTagsWithFileNumbersMap(List<TagLinesDetails> tagLinesDetailsList, int tagLinesCount, int parallelBuildCount) throws Exception;
}
