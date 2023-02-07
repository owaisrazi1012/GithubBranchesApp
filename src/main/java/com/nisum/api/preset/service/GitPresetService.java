package com.nisum.api.preset.service;


import com.nisum.api.preset.domain.dto.GitPresetRequestDTO;
import com.nisum.api.preset.domain.dto.GitPresetResponseDTO;
import com.nisum.exception.custom.GitPresetServiceException;

import java.util.Map;

public interface GitPresetService {
    Map<String, Object> getAllGitPresets(int page, int size) throws GitPresetServiceException;

    GitPresetResponseDTO createGitPreset(GitPresetRequestDTO gitPresetRequest) throws GitPresetServiceException;


    GitPresetResponseDTO getGitPresetDetails(Long id)  throws GitPresetServiceException;

    GitPresetResponseDTO  updateGitPreset(Long id, GitPresetRequestDTO gitPresetRequest) throws GitPresetServiceException;

    void deleteGitPreset(Long id)  throws GitPresetServiceException;
}
