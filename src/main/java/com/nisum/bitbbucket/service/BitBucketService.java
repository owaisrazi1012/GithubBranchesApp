package com.nisum.bitbbucket.service;

import com.nisum.bitbbucket.domain.BitBucketInfoRequest;
import com.nisum.domain.TagLinesDetails;

import java.util.List;
import java.util.Set;

public interface BitBucketService {

    Set<String> loadBranches(BitBucketInfoRequest request);

    Set<String> retrieveAllTags(BitBucketInfoRequest request) throws Exception;

    List<TagLinesDetails> retrieveFeatureLinesForBuild(BitBucketInfoRequest request) throws Exception;
}
