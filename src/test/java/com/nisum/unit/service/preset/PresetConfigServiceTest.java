package com.nisum.unit.service.preset;

import com.nisum.api.preset.domain.dto.*;
import com.nisum.api.preset.domain.entity.*;
import com.nisum.api.preset.repository.*;
import com.nisum.api.preset.service.impl.PresetConfigServiceImpl;
import com.nisum.api.preset.service.impl.PresetMapperServiceImpl;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Arrays;
import java.util.HashSet;

import static org.mockito.Mockito.when;
import static org.springframework.test.util.AssertionErrors.assertEquals;

@SpringBootTest
public class PresetConfigServiceTest {

    @InjectMocks
    PresetConfigServiceImpl presetConfigService;

    @Mock
    PresetMapperServiceImpl presetMapperService;

    @Mock
    private TestPresetRepository testPresetRepository;

    @Mock
    private ExecutionPresetRepository executionPresetRepository;

    @Mock
    private JenkinsPresetRepository jenkinsPresetRepository;

    @Mock
    private NotificationPresetRepository notificationPresetRepository;

    @Mock
    private GitPresetRepository gitPresetRepository;




    @Test
    public void getExecutionEnginePresets(){
        when(testPresetRepository.findAll()).thenReturn(Arrays.asList(getTestPreset()));
        when(executionPresetRepository.findAll()).thenReturn(Arrays.asList(getExecutionPreset()));
        when(jenkinsPresetRepository.findAll()).thenReturn(Arrays.asList(getJenkinsPreset()));
        when(notificationPresetRepository.findAll()).thenReturn(Arrays.asList(getNotificationPreset()));
        when(gitPresetRepository.findAll()).thenReturn(Arrays.asList(getGitPreset()));

        when(presetMapperService.mapTestPresetToDTO(getTestPreset())).thenReturn(getTestPresetResponseDTO());
        when(presetMapperService.mapExecutionPresetToDTO(getExecutionPreset())).thenReturn(getExecutionPresetResponseDTO());
        when(presetMapperService.mapJenkinsPresetToDTO(getJenkinsPreset())).thenReturn(getJenkinsPresetResponseDTO());
        when(presetMapperService.mapNotificationPresetToDTO(getNotificationPreset())).thenReturn(getNotificationPresetResponseDTO());
        when(presetMapperService.mapGitPresetToDTO(getGitPreset())).thenReturn(getGitPresetResponseDTO());


        assertEquals("Same is not Execution Engine Presets",
                presetConfigService.getExecutionEnginePresets(),
                getExecutionEngineDTO());
    }

    private ExecutionEngineDTO getExecutionEngineDTO(){
        return ExecutionEngineDTO.builder()
                .testPresets(Arrays.asList(presetMapperService.mapTestPresetToTestPresetDTO(getTestPreset())))
                .executionPresets(Arrays.asList(presetMapperService.mapExecutionPresetToDTO(getExecutionPreset())))
                .jenkinsPresets(Arrays.asList(presetMapperService.mapJenkinsPresetToDTO(getJenkinsPreset())))
                .notificationPresets(Arrays.asList(presetMapperService.mapNotificationPresetToDTO((getNotificationPreset()))))
                .gitPresets(Arrays.asList(presetMapperService.mapGitPresetToDTO(getGitPreset())))
                .build();
    }

    private TestPreset getTestPreset(){
        return TestPreset.builder()
                .name("Test Preset Name")
                .tags("Tag Preset Name")
                .parallelBuilds(1)
//                .testPresets()
                .build();
    }

    private ExecutionPreset getExecutionPreset(){
        return ExecutionPreset.builder()
                .name("Testing Execution Preset Name")
                .mavenConfigParams(new HashSet<>())
//                .testPresets()
                .build();
    }

    private GitPreset getGitPreset() {
        return GitPreset.builder()
                .repoUrl("https://gitlab.mynisum.com/orazi/tapdemo")
                .userName("orazi")
                .name("orazi")
                .accessToken("yKUKdVHjnN9W9nH15WvU")
                .branch("devellop")
                .build();
    }

    private JenkinsPreset getJenkinsPreset(){
        return JenkinsPreset.builder()
                .name("Name of Jenkins Preset")
                .url("URL of Jenkins Preset")
                .password("Password of Jenkins Preset")
                .credentialId("Credential Id of Jenkins Preset")
                .slave("Slave of Jenkins Preset")
                .build();
    }

    private NotificationPreset getNotificationPreset(){
        return NotificationPreset.builder()
                .name("Name of Jenkins Preset")
                .presetConfig("Preset Config of Jenkins Preset")
                .recipients("Recipients of Jenkins Preset")
                .build();
    }

    private TestPresetResponseDTO getTestPresetResponseDTO(){
        return TestPresetResponseDTO.builder()
                .name("Test Preset Name")
                .tags("Tag Preset Name")
                .parallelBuilds(1)
//                .testPresets()
                .build();
    }

    private ExecutionPresetResponseDTO getExecutionPresetResponseDTO(){
        return ExecutionPresetResponseDTO.builder()
                .name("Testing Execution Preset Name")
                .mavenConfigParams(new HashSet<>())
//                .testPresets()
                .build();
    }

    private GitPresetResponseDTO getGitPresetResponseDTO() {
        return GitPresetResponseDTO.builder()
                .repoUrl("https://gitlab.mynisum.com/orazi/tapdemo")
                .userName("orazi")
                .name("orazi")
                .accessToken("yKUKdVHjnN9W9nH15WvU")
                .branch("devellop")
                .build();
    }

    private JenkinsPresetResponseDTO getJenkinsPresetResponseDTO(){
        return JenkinsPresetResponseDTO.builder()
                .name("Name of Jenkins Preset")
                .url("URL of Jenkins Preset")
                .password("Password of Jenkins Preset")
                .credentialId("Credential Id of Jenkins Preset")
                .slave("Slave of Jenkins Preset")
                .build();
    }

    private NotificationPresetResponseDTO getNotificationPresetResponseDTO(){
        return NotificationPresetResponseDTO.builder()
                .name("Name of Jenkins Preset")
                .presetConfig("Preset Config of Jenkins Preset")
                .recipients("Recipients of Jenkins Preset")
                .build();
    }
}