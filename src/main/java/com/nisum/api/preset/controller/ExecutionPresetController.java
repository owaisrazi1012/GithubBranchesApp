package com.nisum.api.preset.controller;

import com.nisum.api.preset.domain.dto.ExecutionEngineDTO;
import com.nisum.api.preset.domain.dto.ExecutionPresetRequestDTO;
import com.nisum.api.preset.domain.dto.ExecutionPresetResponseDTO;
import com.nisum.api.preset.service.ExecutionPresetService;
import com.nisum.api.preset.service.PresetConfigService;
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
public class ExecutionPresetController {

    @Autowired
    private ExecutionPresetService executionPresetService;

    @Autowired
    private PresetConfigService presetConfigService;


    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_VIEWER') ")
    @GetMapping("/presets")
    public ResponseEntity<Map<String, Object>> getAllPresetsNames() {
        log.info("Received get Preset all Names request");
        ExecutionEngineDTO executionEngineDTO = presetConfigService.getExecutionEnginePresets();
        return new EntityResponseSuccess<>(executionEngineDTO).getResponse();
    }

    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_VIEWER') ")
    @GetMapping("/execution-presets")
    public ResponseEntity<Map<String, Object>> getAllExecutionPresets(@RequestParam int page, @RequestParam int size) {
        log.info("Received execution presets search all request with page no {} and size  {}", page, size);
        Map<String, Object> response = executionPresetService.getAllExecutionPresets(page,size);
        return new EntityResponseSuccess<>(response).getResponse();
    }

    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_VIEWER') ")
    @GetMapping("/execution-presets/{id}")
    public ResponseEntity<Map<String, Object>> getExecutionPresetDetails(@PathVariable("id") Long id) {
        log.info("Received Get Execution Preset Details with id {}" , id);
        ExecutionPresetResponseDTO executionPreset = executionPresetService.getExecutionPresetById(id);
        return new EntityResponseSuccess<>(executionPreset).getResponse();
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PostMapping("/execution-presets")
    public ResponseEntity<Map<String, Object>> createExecutionPreset(@RequestBody ExecutionPresetRequestDTO executionPresetRequest) {
        log.info("Received Create Execution Preset request with name {}" , executionPresetRequest.getName());
        ExecutionPresetResponseDTO newExecutionPresetDto = executionPresetService.save(executionPresetRequest);
        return new EntityResponseSuccess<>(newExecutionPresetDto, Constants.RECORD_CREATED , HttpStatus.CREATED).getResponse();
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PutMapping("/execution-presets/{id}")
    public ResponseEntity<Map<String, Object>> updateExecutionPreset(@PathVariable("id") Long id, @RequestBody ExecutionPresetRequestDTO executionPresetRequest) {
        log.info("Received Update Execution Preset request with id {} , and name {}", id , executionPresetRequest.getName());
        ExecutionPresetResponseDTO executionPresetDto = executionPresetService.updateExecutionPreset(id, executionPresetRequest);
        return new EntityResponseSuccess<>(executionPresetDto, Constants.RECORD_UPDATED).getResponse();
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @DeleteMapping("/execution-presets/{id}")
    public ResponseEntity<Map<String, Object>> deleteExecutionPreset(@PathVariable("id") Long id) {
        log.info("Received Delete Execution Preset request with id {}", id);
        executionPresetService.deleteExecutionPreset(id);

        Map<String,String> messageBody = new HashMap<>();
        messageBody.put("message", Constants.RECORD_DELETED);
        return new EntityResponseSuccess<>( messageBody, Constants.RECORD_DELETED).getResponse();
    }
}
