package com.nisum.api.preset.service.impl;


import com.nisum.api.preset.domain.dto.ExecutionPresetResponseDTO;
import com.nisum.api.preset.domain.dto.GitPresetResponseDTO;
import com.nisum.api.preset.domain.dto.JenkinsPresetResponseDTO;
import com.nisum.api.preset.domain.dto.JiraPresetResponseDTO;
import com.nisum.api.preset.domain.dto.NotificationPresetResponseDTO;
import com.nisum.api.preset.domain.dto.TestPresetResponseDTO;
import com.nisum.api.preset.domain.entity.ExecutionPreset;
import com.nisum.api.preset.domain.entity.GitPreset;
import com.nisum.api.preset.domain.entity.JenkinsPreset;
import com.nisum.api.preset.domain.entity.JiraPreset;
import com.nisum.api.preset.domain.entity.NotificationPreset;
import com.nisum.api.preset.domain.entity.TestPreset;
import com.nisum.api.preset.service.PresetMapperService;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Service
@Profile({"dev", "qa"})
public class PresetMapperServiceImpl implements PresetMapperService {

    @Override
    public TestPresetResponseDTO mapTestPresetToTestPresetDTO(TestPreset testPreset) {
        if (testPreset != null) {
            TestPresetResponseDTO testPresetResponseDTO = new TestPresetResponseDTO();
            testPresetResponseDTO.setTestPresetId(testPreset.getId());
            testPresetResponseDTO.setName(testPreset.getName());
            testPresetResponseDTO.setTags(testPreset.getTags());
            testPresetResponseDTO.setParallelBuilds(testPreset.getParallelBuilds());
            testPresetResponseDTO.setGitPreset(mapGitPresetToDTO(testPreset.getGitPreset()));
            testPresetResponseDTO.setJenkinsPreset(mapJenkinsPresetToDTO(testPreset.getJenkinsPreset()));
            testPresetResponseDTO.setExecutionPreset(mapExecutionPresetToDTO(testPreset.getExecutionPreset()));
            testPresetResponseDTO.setNotificationPreset(mapNotificationPresetToDTO(testPreset.getNotificationPreset()));
            testPresetResponseDTO.setJiraPreset(mapJiraPresetToDTO(testPreset.getJiraPreset()));
            return testPresetResponseDTO;
        }
        return null;
    }

    public TestPresetResponseDTO mapTestPresetToDTO(TestPreset testPreset) {
        if (testPreset != null) {
            TestPresetResponseDTO testPresetResponseDTO = mapTestPresetToTestPresetDTO(testPreset);
            testPresetResponseDTO.setGitPreset(mapGitPresetToDTO(testPreset.getGitPreset()));
            testPresetResponseDTO.setJenkinsPreset(mapJenkinsPresetToDTO(testPreset.getJenkinsPreset()));
            testPresetResponseDTO.setExecutionPreset(mapExecutionPresetToDTO(testPreset.getExecutionPreset()));
            testPresetResponseDTO.setNotificationPreset(mapNotificationPresetToDTO(testPreset.getNotificationPreset()));
            testPresetResponseDTO.setJiraPreset(mapJiraPresetToDTO(testPreset.getJiraPreset()));
            return testPresetResponseDTO;
        }
        return null;
    }

    public ExecutionPresetResponseDTO mapExecutionPresetToDTO(ExecutionPreset executionPreset) {
        if (executionPreset != null) {
            ExecutionPresetResponseDTO executionPresetResponseDTO = new ExecutionPresetResponseDTO();
            executionPresetResponseDTO.setExecutionPresetId(executionPreset.getId());
            executionPresetResponseDTO.setName(executionPreset.getName());
            executionPresetResponseDTO.setMavenConfigParams(executionPreset.getMavenConfigParamsResponse());
            executionPresetResponseDTO.setTestPresets(executionPresetResponseDTO.getTestPresetsSet(executionPreset));
            return executionPresetResponseDTO;
        }
        return null;
    }

    public JenkinsPresetResponseDTO mapJenkinsPresetToDTO(JenkinsPreset jenkinsPreset) {
        if (jenkinsPreset != null) {
            JenkinsPresetResponseDTO jenkinsPresetResponseDTO = new JenkinsPresetResponseDTO();
            jenkinsPresetResponseDTO.setJenkinsPresetId(jenkinsPreset.getId());
            jenkinsPresetResponseDTO.setName(jenkinsPreset.getName());
            jenkinsPresetResponseDTO.setUrl(jenkinsPreset.getUrl());
            jenkinsPresetResponseDTO.setUserName(jenkinsPreset.getUserName());
            jenkinsPresetResponseDTO.setPassword(jenkinsPreset.getPassword());
            jenkinsPresetResponseDTO.setCredentialId(jenkinsPreset.getCredentialId());
            jenkinsPresetResponseDTO.setSlave(jenkinsPreset.getSlave());
            jenkinsPresetResponseDTO.setTestPresets(jenkinsPresetResponseDTO.getTestPresetsSet(jenkinsPreset));
            return jenkinsPresetResponseDTO;
        }
        return null;
    }

    public GitPresetResponseDTO mapGitPresetToDTO(GitPreset gitPreset) {
        if (gitPreset != null) {
            GitPresetResponseDTO gitPresetResponseDTO = new GitPresetResponseDTO();
            gitPresetResponseDTO.setGitPresetId(gitPreset.getId());
            gitPresetResponseDTO.setName(gitPreset.getName());
            gitPresetResponseDTO.setRepoUrl(gitPreset.getRepoUrl());
            gitPresetResponseDTO.setUserName(gitPreset.getUserName());
            gitPresetResponseDTO.setAccessToken(gitPreset.getAccessToken());
            gitPresetResponseDTO.setBranch(gitPreset.getBranch());
            gitPresetResponseDTO.setTestPresets(gitPresetResponseDTO.getTestPresetsSet(gitPreset));
            return gitPresetResponseDTO;
        }
        return null;
    }

    public NotificationPresetResponseDTO mapNotificationPresetToDTO(NotificationPreset notificationPreset) {
        if (notificationPreset != null) {
            NotificationPresetResponseDTO notificationPresetResponseDTO = new NotificationPresetResponseDTO();
            notificationPresetResponseDTO.setNotificationPresetId(notificationPreset.getId());
            notificationPresetResponseDTO.setName(notificationPreset.getName());
            notificationPresetResponseDTO.setPresetConfig(notificationPreset.getPresetConfig());
            notificationPresetResponseDTO.setRecipients(notificationPreset.getRecipients());
            notificationPresetResponseDTO.setTestPresets(notificationPresetResponseDTO.getTestPresetsSet(notificationPreset));
            return notificationPresetResponseDTO;
        }
        return null;
    }

    public JiraPresetResponseDTO mapJiraPresetToDTO(JiraPreset jiraPreset) {
        if (jiraPreset != null) {
            JiraPresetResponseDTO jiraPresetResponseDTO = new JiraPresetResponseDTO();
            jiraPresetResponseDTO.setJiraPresetId(jiraPreset.getId());
            jiraPresetResponseDTO.setUserName(jiraPreset.getUserName());
            jiraPresetResponseDTO.setToken(jiraPreset.getToken());
            jiraPresetResponseDTO.setProjectId(jiraPreset.getProjectId());
            jiraPresetResponseDTO.setTestPresets(jiraPresetResponseDTO.getTestPresetsSet(jiraPreset));
            return jiraPresetResponseDTO;
        }
        return null;
    }


}