package com.nisum.api.preset.service;

import com.nisum.api.preset.domain.dto.NotificationPresetRequestDTO;
import com.nisum.api.preset.domain.dto.NotificationPresetResponseDTO;
import com.nisum.exception.custom.NotificationPresetServiceException;

import java.util.Map;

public interface NotificationPresetService {
    Map<String, Object> getAllNotificationPresets(int page, int size) throws NotificationPresetServiceException;

    NotificationPresetResponseDTO getNotificationPresetDetails(Long id) throws NotificationPresetServiceException;

    NotificationPresetResponseDTO createNotificationPreset(NotificationPresetRequestDTO notificationPresetRequest) throws NotificationPresetServiceException;

    NotificationPresetResponseDTO updateNotificationPreset(Long id, NotificationPresetRequestDTO notificationPresetRequest) throws NotificationPresetServiceException;

    void deleteNotificationPreset(Long id) throws NotificationPresetServiceException;
}
