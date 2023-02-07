package com.nisum.api.preset.repository;

import com.nisum.api.preset.domain.entity.JenkinsPreset;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface JenkinsPresetRepository extends PagingAndSortingRepository<JenkinsPreset, Long> {

}
