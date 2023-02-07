package com.nisum.api.preset.repository;

import com.nisum.api.preset.domain.entity.MavenConfigParam;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MavenConfigParamRepository extends JpaRepository<MavenConfigParam, Long> {

}
