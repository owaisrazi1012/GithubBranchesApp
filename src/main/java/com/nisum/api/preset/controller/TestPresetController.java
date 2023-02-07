package com.nisum.api.preset.controller;

import com.nisum.api.preset.domain.dto.TestPresetRequestDTO;
import com.nisum.api.preset.domain.dto.TestPresetResponseDTO;
import com.nisum.api.preset.service.PresetMapperService;
import com.nisum.api.preset.service.TestingPresetService;
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
public class TestPresetController {

    @Autowired
    private TestingPresetService testingPresetService;

    @Autowired
    private PresetMapperService presetMapperService;

    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_VIEWER') ")
    @GetMapping("/test-presets")
    public ResponseEntity<Map<String, Object>> getAllTestPresets(@RequestParam int page, @RequestParam int size) {
        log.info("Received get Test Preset all with page no {} and size {}",page ,size);
        Map<String, Object> response = testingPresetService.getAllTestPresets(page, size);
        return new EntityResponseSuccess<>(response).getResponse();

    }

    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_VIEWER') ")
    @GetMapping("/test-presets/{id}")
    public ResponseEntity<Map<String, Object>> getTestPresetDetails(@PathVariable("id") Long id) {
        log.info("Received get Test Preset Details request with id {}", id);
        TestPresetResponseDTO testPresetResponseDTO = testingPresetService.getTestPresetDetails(id);
        return new EntityResponseSuccess<>(testPresetResponseDTO).getResponse();
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PostMapping("/test-presets")
    public ResponseEntity<Map<String, Object>> createTestPreset(@RequestBody TestPresetRequestDTO testPresetRequest) {
        log.info("Received Create Test Preset request with name {}", testPresetRequest.getName());
        TestPresetResponseDTO testPresetResponseDTO = testingPresetService.createTestPreset(testPresetRequest);
        return new EntityResponseSuccess<>(testPresetResponseDTO, Constants.RECORD_CREATED ,HttpStatus.CREATED).getResponse();
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PutMapping("/test-presets/{id}")
    public ResponseEntity<Map<String, Object>> updateTestPreset(@PathVariable("id") Long id, @RequestBody TestPresetRequestDTO testPresetRequest) {
        log.info("Received Update Test Preset request with id {} and name {}", id, testPresetRequest.getName());
        TestPresetResponseDTO testPresetResponseDTO = testingPresetService.updateTestPreset(id, testPresetRequest);
        return new EntityResponseSuccess<>(testPresetResponseDTO, Constants.RECORD_UPDATED).getResponse();
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @DeleteMapping("/test-presets/{id}")
    public ResponseEntity<Map<String, Object>> deleteTestPreset(@PathVariable("id") Long id) {
        log.info("Received Delete Test Preset request with id {}", id);
        testingPresetService.deleteTestPreset(id);

        Map<String, Object> messageBody = new HashMap<>();
        messageBody.put("message", Constants.RECORD_DELETED);

        return new EntityResponseSuccess<>(messageBody, Constants.RECORD_DELETED).getResponse();
    }
}
