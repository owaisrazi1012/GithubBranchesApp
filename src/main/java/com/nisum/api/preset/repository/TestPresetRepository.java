package com.nisum.api.preset.repository;

import com.nisum.api.preset.domain.entity.TestPreset;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TestPresetRepository extends JpaRepository<TestPreset, Long> {

    TestPreset findByName(String name);
    List<TestPreset> findAllByOrderByIdDesc();

}
