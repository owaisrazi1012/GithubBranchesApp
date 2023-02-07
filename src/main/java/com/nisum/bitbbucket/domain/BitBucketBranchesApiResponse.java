package com.nisum.bitbbucket.domain;

import lombok.Data;

import java.util.List;

@Data
public class BitBucketBranchesApiResponse {

    List<BitBucketBranches> values;
}
