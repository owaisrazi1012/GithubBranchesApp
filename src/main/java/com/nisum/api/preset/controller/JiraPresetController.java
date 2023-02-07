package com.nisum.api.preset.controller;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import com.nisum.api.preset.domain.dto.JiraPresetRequestDTO;
import com.nisum.api.preset.domain.dto.JiraPresetResponseDTO;
import com.nisum.exception.custom.JiraPresetServiceException;
import com.nisum.api.preset.service.JiraPresetService;
import com.nisum.util.Constants;
import java.util.HashMap;
import java.util.Map;

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

@CrossOrigin("*")
@RestController
@Slf4j
public class JiraPresetController {

    @Autowired
    private JiraPresetService jiraPresetService;

    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_VIEWER') ")
    @GetMapping("/jira-presets")
    public ResponseEntity<Map<String, Object>> getAllJiraPresets(@RequestParam int page, @RequestParam int size)
            throws JiraPresetServiceException
    {
        log.info("Received get All Jira Presets with page no {} and size {}", page, size);
        Map<String, Object> response = jiraPresetService.getAllJiraPresets(page, size);
        return new EntityResponseSuccess<>(response).getResponse();
    }

    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_VIEWER') ")
    @GetMapping(path = "/jira-presets/{id}" ,produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> getJiraPresetDetails(@PathVariable("id") Long id)
            throws JiraPresetServiceException {
        log.info("Received get Jira Preset Details with id {}", id);
        JiraPresetResponseDTO jiraPresetResponseDTO = jiraPresetService.getJiraPresetDetails(id);
        return new EntityResponseSuccess<>(jiraPresetResponseDTO).getResponse();
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PostMapping("/jira-presets")
    public ResponseEntity<Map<String, Object>> createJiraPreset(@RequestBody JiraPresetRequestDTO jiraPresetRequestDTO)
            throws JiraPresetServiceException {
        log.info("Received create Jira Preset with name {}" , jiraPresetRequestDTO.getUserName());
        JiraPresetResponseDTO jiraPresetResponseDTO = jiraPresetService.createJiraPreset(jiraPresetRequestDTO);
        return new EntityResponseSuccess<>(jiraPresetResponseDTO, Constants.RECORD_CREATED , HttpStatus.CREATED).getResponse();
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PutMapping("/jira-presets/{id}")
    public ResponseEntity<Map<String, Object>> updateJiraPreset(@PathVariable("id") Long id, @RequestBody JiraPresetRequestDTO jiraPresetRequestDTO)
            throws JiraPresetServiceException {
        log.info("Received update Jira Preset request with id {} and name {}" , id, jiraPresetRequestDTO.getUserName());
        JiraPresetResponseDTO jiraPresetResponseDTO = jiraPresetService.updateJiraPreset(id, jiraPresetRequestDTO);
        return new EntityResponseSuccess<>(jiraPresetResponseDTO, Constants.RECORD_UPDATED).getResponse();

    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @DeleteMapping("/jira-presets/{id}")
    public ResponseEntity<Map<String, Object>> deleteJiraPreset(@PathVariable("id") Long id)
            throws JiraPresetServiceException {
        log.info("Received delete Jira Preset with id {}", id);
        jiraPresetService.deleteJiraPreset(id);
        Map<String,String> messageBody = new HashMap<>();
        messageBody.put("message", Constants.RECORD_DELETED);

        return new EntityResponseSuccess<>(messageBody, Constants.RECORD_DELETED).getResponse();
    }
}
