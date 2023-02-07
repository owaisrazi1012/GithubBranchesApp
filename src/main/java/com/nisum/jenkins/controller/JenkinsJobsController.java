package com.nisum.jenkins.controller;

import com.nisum.exception.custom.IncorrectInputException;
import com.nisum.exception.custom.NoRecordFoundException;
import com.nisum.jenkins.domain.JenkinsBuildInfo;
import com.nisum.jenkins.domain.JenkinsJob;
import com.nisum.jenkins.domain.JenkinsJobInput;
import com.nisum.jenkins.domain.Response;
import com.nisum.jenkins.service.JenkinsJobsService;
import java.util.Map;
import com.nisum.exception.handling.EntityResponseFailure;
import com.nisum.exception.handling.EntityResponseSuccess;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@CrossOrigin("*")
@RestController
@RequestMapping("/api/v1/jenkins")
public class JenkinsJobsController {

    @Autowired
    private JenkinsJobsService jenkinsJobsService;

    private static final Logger LOGGER = LoggerFactory.getLogger(JenkinsJobsController.class);

    /*
     * Get all Jenkins jobs
     */
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_VIEWER') ")
    @GetMapping(value = "/jobs")
    public ResponseEntity<Map<String, Object>> getUserJenkinsJobs() {
        LOGGER.info("Fetching all Jenkin Jobs");
        try {
            List<JenkinsJob> jobDetails = jenkinsJobsService.fetchAllJenkinsJobs();
            return new EntityResponseSuccess<>(jobDetails).getResponse();
        } catch (Exception e) {
            LOGGER.error("Exception occurred on retrieving all Jenkins jobs {}", e.getMessage());
            return new EntityResponseFailure("error on retrieving all Jenkins Jobs " + e.getMessage()).getResponse();
        }
    }

    /*
     * Create new Jenkins Job
     */
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_VIEWER') ")
    @RequestMapping(value = "/jobs/{jobName}/create", produces = "application/json")
    public ResponseEntity<Map<String, Object>> createNewJenkinsJob(
            @PathVariable(name = "jobName") String jobName) {
        LOGGER.info("Creating new Jenkins Job. JobName : {}", jobName);
        try {
            Response response = jenkinsJobsService.createJenkinsJob(jobName);
            return new EntityResponseSuccess<>(response).getResponse();
        } catch (Exception e) {
            LOGGER.error("Exception occurred on Creating new Jenkins Job JobName : {}, exception : {}",
                    jobName, e.getMessage());
            return new EntityResponseFailure("exception occurred on creating Jenkins Job " + e.getMessage()).getResponse();
        }
    }

    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_VIEWER') ")
    @PostMapping(value = "/build")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> triggerJenkinsJob(@RequestBody JenkinsJobInput jenkinsJobInput) {
        LOGGER.info("Triggering the Jenkins Job. {}", jenkinsJobInput);
        try {
            Response serviceResponse = jenkinsJobsService.triggerJenkinsJob(jenkinsJobInput);
            return new EntityResponseSuccess<>(serviceResponse).getResponse();
        } catch (IncorrectInputException e) {
            LOGGER.error("Exception occurred {}", e.getMessage());
            return new EntityResponseFailure(e.getMessage(), HttpStatus.BAD_REQUEST).getResponse();
        } catch (Exception e) {
            LOGGER.error("Exception occurred on triggering Jenkins Job, exception : {}",
                    e.getMessage());
            return new EntityResponseFailure("Exception occurred on triggering Jenkins Job, " + e.getMessage()).getResponse();
        }

    }

    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_VIEWER') ")
    @GetMapping(value = "/jobs/{jobName}/last-build-info", produces = "application/json")
    public ResponseEntity<Map<String, Object>> getJenkinsLastJobBuildStatus
            (@PathVariable(name = "jobName") String jobName) {
        LOGGER.info("Fetching latest build status of Job : {}", jobName);
        try {
            JenkinsBuildInfo jenkinsBuildInfo = jenkinsJobsService.fetchLatestBuildInfo(jobName);
            return new EntityResponseSuccess<>(jenkinsBuildInfo).getResponse();
        } catch (NoRecordFoundException e) {
            LOGGER.error("No build found for the Job : {}", jobName);
            return new EntityResponseFailure("No build found for the Job : " + jobName).getResponse();
        } catch (Exception e) {
            LOGGER.error("Exception occurred on getting build status of Job : {}, exception : {}",
                    jobName, e.getMessage());
            return new EntityResponseFailure("Exception occurred on getting build status " + e.getMessage()).getResponse();
        }
    }

    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_VIEWER') ")
    @GetMapping(value = "/all-builds-info", produces = "application/json")
    public ResponseEntity<Map<String, Object>> getAllJenkinsJobBuildStatus
            (@RequestParam int page, @RequestParam int size) {
        LOGGER.info("Fetching latest build status of Job ");
        try {
            Map<String, Object> jenkinsBuildInfo = jenkinsJobsService.fetchAllBuildInfo(page, size);
            return new EntityResponseSuccess<>(jenkinsBuildInfo).getResponse();
        } catch (Exception e) {
            LOGGER.error("Exception occurred on getting build info of Job , exception : {}",
                    e.getMessage());
            return new EntityResponseFailure("Exception occurred on getting build status " + e.getMessage()).getResponse();
        }
    }


    @GetMapping(value = "/jobs/{jobName}/{buildNumber}/logs", produces = "application/json")
    public ResponseEntity<Resource> downloadBuildLogs(@PathVariable(name = "jobName") String jobName,
                                                      @PathVariable(name = "buildNumber") int buildNumber) {
        LOGGER.info("Downloading logs for build # {} of job: {}", buildNumber, jobName);
        try {
            File file = jenkinsJobsService.downloadLogs(jobName, buildNumber);
            Path path = Paths.get(file.getAbsolutePath());
            ByteArrayResource resource = new ByteArrayResource(Files.readAllBytes(path));

            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; " + file.getName());
            headers.add("Cache-Control", "no-cache, no-store, must-revalidate");
            headers.add("Pragma", "no-cache");
            headers.add("Expires", "0");

            LOGGER.info("Build Logs Downloaded Successfully");

            return ResponseEntity.ok()
                    .headers(headers)
                    .contentLength(file.length())
                    .contentType(MediaType.APPLICATION_CBOR)
                    .body(resource);
        } catch (Exception e) {
            LOGGER.error("Exception occurred on downloading build logs, exception : {}", e.getMessage());
            return null;
        }
    }

}

