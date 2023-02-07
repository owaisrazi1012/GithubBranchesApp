package com.nisum.api.preset.controller;


import com.nisum.api.preset.domain.dto.GitPresetRequestDTO;
import com.nisum.exception.custom.GitPresetServiceException;
import com.nisum.api.preset.service.GitPresetService;
import com.nisum.util.Constants;
import com.nisum.exception.handling.EntityResponseSuccess;
import lombok.extern.slf4j.Slf4j;
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

import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;



@CrossOrigin("*")
@RestController
@Slf4j
public class GitPresetController {

    private final GitPresetService gitPresetService;

    public GitPresetController(GitPresetService gitPresetService) {
        this.gitPresetService = gitPresetService;
    }


    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_VIEWER') ")
    @GetMapping(path="/git-presets" ,produces = APPLICATION_JSON_VALUE)
     public ResponseEntity<Map<String, Object>> getAllGitPresets(@RequestParam int page, @RequestParam int size) throws
            GitPresetServiceException {
        log.info("Received Git Preset search all request ");
        return new EntityResponseSuccess<>(gitPresetService.getAllGitPresets(page,size)).getResponse();

    }

    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_VIEWER') ")
    @GetMapping(path="/git-presets/{id}",produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> getGitPresetDetails(@PathVariable("id") Long id) throws
            GitPresetServiceException {
        log.info("Received Git Preset with preset id {} " , id);
        return new EntityResponseSuccess<>(gitPresetService.getGitPresetDetails(id)).getResponse();

    }


    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PostMapping(path="/git-presets",produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> createGitPreset(@RequestBody GitPresetRequestDTO gitPresetRequest)  throws
            GitPresetServiceException {

        log.info("Received Git Preset insert request with userName {} " +
                        " and repoUrl as {}",
                gitPresetRequest.getUserName(), gitPresetRequest.getRepoUrl());
        return new EntityResponseSuccess<>(
                gitPresetService.createGitPreset(gitPresetRequest), Constants.RECORD_CREATED , CREATED).getResponse();

    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PutMapping(path="/git-presets/{id}",produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> updateGitPreset(@PathVariable("id") Long id, @RequestBody GitPresetRequestDTO
            gitPresetRequest) throws GitPresetServiceException {

            log.info("Received Git Preset update request with id {} " , id);
            return new EntityResponseSuccess<>(
                    gitPresetService.updateGitPreset(id,gitPresetRequest), Constants.RECORD_UPDATED).getResponse();


    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @DeleteMapping(path="/git-presets/{id}")
    public ResponseEntity<Map<String, Object>> deleteGitPreset(@PathVariable("id") Long id) {

        log.info("Received Git Preset delete request with id {} " , id);
        gitPresetService.deleteGitPreset(id);

        Map<String, Object> messageBody = new HashMap<>();
        messageBody.put("message", Constants.RECORD_DELETED);
        return new EntityResponseSuccess<>(messageBody, Constants.RECORD_DELETED).getResponse();

    }
}
