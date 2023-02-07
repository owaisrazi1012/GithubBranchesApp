package com.nisum.api.preset.repository;

import com.nisum.api.preset.domain.entity.NotificationPreset;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface NotificationPresetRepository extends PagingAndSortingRepository<NotificationPreset, Long> {

}
