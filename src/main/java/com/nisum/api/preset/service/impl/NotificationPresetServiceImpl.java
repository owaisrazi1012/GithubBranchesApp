package com.nisum.api.preset.service.impl;

import com.nisum.api.preset.domain.dto.NotificationPresetRequestDTO;
import com.nisum.api.preset.domain.dto.NotificationPresetResponseDTO;
import com.nisum.api.preset.domain.entity.NotificationPreset;
import com.nisum.exception.custom.NotificationPresetServiceException;
import com.nisum.api.preset.repository.NotificationPresetRepository;
import com.nisum.api.preset.repository.TestPresetRepository;
import com.nisum.api.preset.service.NotificationPresetService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.stream.Collectors;

import static com.nisum.util.Constants.NO_RECORD_FOUND;
import static org.springframework.http.HttpStatus.NOT_ACCEPTABLE;


@Service
@Profile({"dev", "qa"})
public class NotificationPresetServiceImpl implements NotificationPresetService {


    @Autowired
    private NotificationPresetRepository notificationPresetRepository;

    @Autowired
    private TestPresetRepository testPresetRepository;

    @Override
    public Map<String, Object> getAllNotificationPresets(int page, int size) throws NotificationPresetServiceException {
        Page<NotificationPreset> notificationPresetPage =
                notificationPresetRepository.findAll(PageRequest.of(page, size, Sort.by("id").descending()));
        return getNotificationPresetResponseMap(size, notificationPresetPage);
    }

    @Override
    public NotificationPresetResponseDTO getNotificationPresetDetails(Long id) throws NotificationPresetServiceException {

        NotificationPreset notificationPreset = notificationPresetRepository.findById(id)
                .orElseThrow(new NotificationPresetServiceException(NOT_ACCEPTABLE.value(), NO_RECORD_FOUND, new Throwable()));
        return new NotificationPresetResponseDTO(notificationPreset);
    }

    @Override
    public NotificationPresetResponseDTO createNotificationPreset(NotificationPresetRequestDTO notificationPresetRequest)
            throws NotificationPresetServiceException {
        NotificationPreset newNotificationPreset = notificationPresetRepository
                .save(NotificationPreset.builder()
                        .name(notificationPresetRequest.getName())
                        .presetConfig(notificationPresetRequest.getPresetConfig())
                        .recipients(notificationPresetRequest.getRecipients())
                        .testPresets(notificationPresetRequest.getTestPresetId()!= null ?
                                Collections.singleton(testPresetRepository.findById(notificationPresetRequest.getTestPresetId()).get()) : null)
                        .build());
        return new NotificationPresetResponseDTO(newNotificationPreset);
    }

    @Override
    public NotificationPresetResponseDTO updateNotificationPreset(Long id, NotificationPresetRequestDTO notificationPresetRequest)
            throws NotificationPresetServiceException {

        NotificationPreset notificationPreset = notificationPresetRepository.findById(id)
                .orElseThrow(new NotificationPresetServiceException(NOT_ACCEPTABLE.value(),NO_RECORD_FOUND,new Throwable()));

        notificationPreset.setName(notificationPresetRequest.getName());
        notificationPreset.setPresetConfig(notificationPresetRequest.getPresetConfig());
        notificationPreset.setRecipients(notificationPresetRequest.getRecipients());
        if (notificationPresetRequest.getTestPresetId() != null) {
            notificationPreset.setTestPresets(Collections.singleton(testPresetRepository.findById(notificationPresetRequest.getTestPresetId()).get()));
        }
        return new NotificationPresetResponseDTO(notificationPresetRepository.save(notificationPreset));
    }

    @Override
    public void deleteNotificationPreset(Long id)  throws NotificationPresetServiceException {
        try {
            notificationPresetRepository.deleteById(id);
        } catch (EmptyResultDataAccessException ex) {
            throw new NotificationPresetServiceException(NOT_ACCEPTABLE.value(),NO_RECORD_FOUND,new Throwable());
        }
    }

    private Map<String, Object> getNotificationPresetResponseMap(int size, Page<NotificationPreset> notificationPresetPage) {
        Map<String, Object> response = new HashMap<>();
        if (notificationPresetPage.hasContent()) {
            response.put("notificationPresets", 
                    notificationPresetPage.getContent().stream()
                            .map(NotificationPresetResponseDTO::new)
                            .collect(Collectors.toCollection(LinkedHashSet::new)));
            response.put("currentPage",  notificationPresetPage.getNumber());
            response.put("limit",  size);
            response.put("totalRecords",  notificationPresetPage.getTotalElements());
            response.put("totalPages",  notificationPresetPage.getTotalPages());
        } else { //populate empty map
            response.put("notificationPresets",  new HashSet<>());
            response.put("currentPage",  0);
            response.put("limit",  size);
            response.put("totalRecords",  0l);
            response.put("totalPages",  0);
        }
        return response;
    }
}
