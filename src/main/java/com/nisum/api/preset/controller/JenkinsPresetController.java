package com.nisum.api.preset.controller;

import com.nisum.api.preset.domain.dto.JenkinsPresetRequestDTO;
import com.nisum.api.preset.domain.dto.JenkinsPresetResponseDTO;
import com.nisum.api.preset.service.JenkinsPresetService;
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


@CrossOrigin("*")
@RestController
@Slf4j
public class JenkinsPresetController {

    @Autowired
    private JenkinsPresetService jenkinsPresetService;


    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_VIEWER') ")
    @GetMapping("/jenkins-presets")
    public ResponseEntity<Map<String, Object>>  getAllJenkinsPresets(@RequestParam int page, @RequestParam int size) {
        log.info("Received get Jenkins Preset with page: {} and size: {}", page , size);
        Map<String, Object> response = jenkinsPresetService.getAllJenkinsPresets(page, size);
        return new EntityResponseSuccess<>(response).getResponse();

    }

    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_VIEWER') ")
    @GetMapping("/jenkins-presets/{id}")
    public ResponseEntity<Map<String, Object>>  getJenkinsPresetDetails(@PathVariable("id") Long id) {
        log.info("Received get Jenkins Preset Details with id: {}", id);
        JenkinsPresetResponseDTO jenkinsPresetResponseDTO = jenkinsPresetService.getJenkinsPresetDetails(id);
        return new EntityResponseSuccess<>(jenkinsPresetResponseDTO).getResponse();

    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PostMapping("/jenkins-presets")
    public ResponseEntity<Map<String, Object>>  createJenkinsPreset(@RequestBody JenkinsPresetRequestDTO jenkinsPresetRequest) {
        log.info("Received Create Jenkins Preset with name: {}, url: {} and username: {}",
                jenkinsPresetRequest.getName(), jenkinsPresetRequest.getUrl(), jenkinsPresetRequest.getUserName());
        JenkinsPresetResponseDTO jenkinsPreset = jenkinsPresetService.createJenkinsPreset(jenkinsPresetRequest);
        return new EntityResponseSuccess<>(jenkinsPreset,Constants.RECORD_CREATED , HttpStatus.CREATED).getResponse();
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PutMapping("/jenkins-presets/{id}")
    public ResponseEntity<Map<String, Object>> updateJenkinsPreset(@PathVariable("id") Long id, @RequestBody JenkinsPresetRequestDTO jenkinsPresetRequest) {
        log.info("Received Update Jenkins Preset request with id {}, name: {}, url: {} and username: {}",
                id, jenkinsPresetRequest.getName(), jenkinsPresetRequest.getUrl(), jenkinsPresetRequest.getUserName());
        JenkinsPresetResponseDTO jenkinsPreset = jenkinsPresetService.updateJenkinsPreset(id, jenkinsPresetRequest);
        return new EntityResponseSuccess<>(jenkinsPreset, Constants.RECORD_UPDATED).getResponse();

    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @DeleteMapping("/jenkins-presets/{id}")
    public ResponseEntity<Map<String, Object>> deleteJenkinsPreset(@PathVariable("id") Long id) {
        log.info("Received Delete Jenkins Preset request with id {}" , id);
        jenkinsPresetService.deleteJenkinsPreset(id);

        Map<String,String> messageBody = new HashMap<>();
        messageBody.put("message", Constants.RECORD_DELETED);

        return new EntityResponseSuccess<>(messageBody, Constants.RECORD_DELETED).getResponse();
    }
}
