package com.nisum.api.preset.repository;

import com.nisum.api.preset.domain.entity.JiraPreset;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface JiraPresetRepository extends JpaRepository<JiraPreset,Long> {
}
