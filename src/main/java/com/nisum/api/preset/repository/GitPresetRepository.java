package com.nisum.api.preset.repository;

import com.nisum.api.preset.domain.entity.GitPreset;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GitPresetRepository extends PagingAndSortingRepository<GitPreset, Long> {


}
