package com.nisum.api.preset.service;

import com.nisum.api.preset.domain.dto.ExecutionPresetRequestDTO;
import com.nisum.api.preset.domain.dto.ExecutionPresetResponseDTO;

import java.util.List;
import java.util.Map;


public interface ExecutionPresetService {

   List<ExecutionPresetResponseDTO> getExecutionPreset();
   ExecutionPresetResponseDTO getExecutionPresetById(Long id);

    ExecutionPresetResponseDTO save(ExecutionPresetRequestDTO executionPresetRequest);

    Map<String, Object> getAllExecutionPresets(int page, int size);

    ExecutionPresetResponseDTO updateExecutionPreset(Long id, ExecutionPresetRequestDTO executionPresetRequest);

    void deleteExecutionPreset(Long id);
}