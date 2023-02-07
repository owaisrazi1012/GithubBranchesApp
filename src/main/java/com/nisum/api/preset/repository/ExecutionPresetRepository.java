package com.nisum.api.preset.repository;

import com.nisum.api.preset.domain.entity.ExecutionPreset;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface ExecutionPresetRepository extends PagingAndSortingRepository<ExecutionPreset, Long> {

}
