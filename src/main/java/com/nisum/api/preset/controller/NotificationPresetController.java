package com.nisum.api.preset.controller;

import com.nisum.api.preset.domain.dto.NotificationPresetRequestDTO;
import com.nisum.api.preset.domain.dto.NotificationPresetResponseDTO;
import com.nisum.exception.custom.NotificationPresetServiceException;
import com.nisum.api.preset.service.NotificationPresetService;
import com.nisum.util.Constants;
import com.nisum.exception.handling.EntityResponseSuccess;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;


@CrossOrigin("*")
@RestController
@Slf4j
public class NotificationPresetController {

    @Autowired
    private NotificationPresetService notificationPresetService;

    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_VIEWER') ")
    @GetMapping("/notification-presets")
    public ResponseEntity<Map<String, Object>> getAllNotificationPresets(@RequestParam int page, @RequestParam int size)
        throws NotificationPresetServiceException
    {
        log.info("Received get All Notification Presets with page no {} and size {}", page, size);
        Map<String, Object> response = notificationPresetService.getAllNotificationPresets(page, size);
        return new EntityResponseSuccess<>(response).getResponse();
    }

    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_VIEWER') ")
    @GetMapping(path = "/notification-presets/{id}" ,produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> getNotificationPresetDetails(@PathVariable("id") Long id)
            throws NotificationPresetServiceException {
        log.info("Received get Notification Preset Details with id {}", id);
        NotificationPresetResponseDTO notificationPresetResponseDTO = notificationPresetService.getNotificationPresetDetails(id);
        return new EntityResponseSuccess<>(notificationPresetResponseDTO).getResponse();
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PostMapping("/notification-presets")
    public ResponseEntity<Map<String, Object>> createNotificationPreset(@RequestBody NotificationPresetRequestDTO notificationPresetRequest)
            throws NotificationPresetServiceException {
        log.info("Received create Notification Preset with name {}" , notificationPresetRequest.getName());
        NotificationPresetResponseDTO notificationPreset = notificationPresetService.createNotificationPreset(notificationPresetRequest);
        return new EntityResponseSuccess<>(notificationPreset, Constants.RECORD_CREATED ,HttpStatus.CREATED).getResponse();
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PutMapping("/notification-presets/{id}")
    public ResponseEntity<Map<String, Object>> updateNotificationPreset(@PathVariable("id") Long id, @RequestBody NotificationPresetRequestDTO notificationPresetRequest)
            throws NotificationPresetServiceException {
        log.info("Received update Notification Preset request with id {} and name {}" , id, notificationPresetRequest.getName());
        NotificationPresetResponseDTO notificationPreset = notificationPresetService.updateNotificationPreset(id, notificationPresetRequest);
        return new EntityResponseSuccess<>(notificationPreset, Constants.RECORD_UPDATED).getResponse();

    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @DeleteMapping("/notification-presets/{id}")
    public ResponseEntity<Map<String, Object>> deleteNotificationPreset(@PathVariable("id") Long id)
            throws NotificationPresetServiceException {
        log.info("Received delete Notification Preset with id {}", id);
        notificationPresetService.deleteNotificationPreset(id);
        Map<String,String> messageBody = new HashMap<>();
        messageBody.put("message", Constants.RECORD_DELETED);

        return new EntityResponseSuccess<>(messageBody, Constants.RECORD_DELETED).getResponse();
    }
}
