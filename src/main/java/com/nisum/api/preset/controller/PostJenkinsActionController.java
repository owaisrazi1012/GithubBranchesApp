package com.nisum.api.preset.controller;

import com.cdancy.jenkins.rest.domain.job.BuildInfo;
import com.nisum.elasticsearch.service.ElasticSearchService;
import com.nisum.jenkins.service.JenkinsJobsService;
import com.nisum.service.PostJenkinsActionService;
import com.nisum.exception.handling.EntityResponseFailure;
import com.nisum.exception.handling.EntityResponseSuccess;
import io.swagger.v3.oas.annotations.Operation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
public class PostJenkinsActionController {

    private static final Logger LOGGER = LoggerFactory.getLogger(PostJenkinsActionController.class);


    @Autowired
    private PostJenkinsActionService postJenkinsActionService;

    @Autowired
    JenkinsJobsService jenkinsJobsService;

    @Autowired
    private ElasticSearchService elasticSearchService;

    @CrossOrigin("*")
    @Operation(
            summary = "API for all the post actions after jenkins build execution",
            description = "API for all the post actions after jenkins build execution like creating Elk Index for cucumber json file, send email notification with build status and jira creation"
    )
    @PostMapping(value = "/post-jenkins-actions", consumes = {"multipart/form-data"})
    public ResponseEntity<Map<String, Object>> postJenkinsJobActions(@RequestPart("file") MultipartFile file,
                                                                     @RequestPart(name = "attachment", required = false) MultipartFile attachment,
                                                                     @RequestParam("project") String project,
                                                                     @RequestParam("buildDate") long buildDate,
                                                                     @RequestParam("buildNumber") int buildNumber) {
        LOGGER.info("Request received to for jenkins post actions");
        try {
            BuildInfo buildInfo = jenkinsJobsService.fetchBuildInfo(project, buildNumber);
            List<String> responses = new ArrayList<>();
            Map<String, Boolean> elkInsertStatusMap = null;
            if (buildInfo != null) {

                elkInsertStatusMap = elasticSearchService.insertData(file, project, buildDate, buildNumber);

                boolean isActionDone = elkInsertStatusMap.get("isInserted");
                boolean isFailureScenarioExist = elkInsertStatusMap.get("isFailedScenariosExist");

                responses.add("elasticSearchService -- insert data complete") ;

                if (isActionDone) {
                    LOGGER.info("Post Action: Cucumber Json Data inserted Successfully");
                    responses.add("Post Action: Cucumber Json Data inserted Successfully");
                } else {
                    LOGGER.info("Post Action: Cucumber Json Data insertion Failed");
                    responses.add("Post Action: Cucumber Json Data inserted Successfully");
                }
                isActionDone = postJenkinsActionService.saveJenkinsBuildInfo(buildInfo, project, buildDate,
                        "attachment", isFailureScenarioExist);
                if (isActionDone) {
                    LOGGER.info("Post Action: Email/Jira Content Saved Successfully");
                    responses.add("Post Action: Email/Jira Content Saved Successfully");
                } else {
                    LOGGER.info("Post Action: Email/Jira Content Saving Failed");
                    responses.add("Post Action: Email/Jira Content Saving Failed");
                }
            } else {
                LOGGER.info("Post Action: No Build Found with Build # {}", buildNumber);
                responses.add("Post Action: No Build Found with Build # " + buildNumber);
            }

            return new EntityResponseSuccess<>(responses, HttpStatus.ACCEPTED).getResponse();
        } catch (Exception e) {
            LOGGER.info("Exception occurred on Post Jenkins Actions ", e);
            return new EntityResponseFailure(e.getMessage(), HttpStatus.BAD_GATEWAY).getResponse();
        }
    }
}