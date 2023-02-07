package com.nisum.elasticsearch.controller;

import com.nisum.elasticsearch.domain.ElasticCucumberJson;
import com.nisum.elasticsearch.domain.ProjectsWithBuilds;
import com.nisum.elasticsearch.service.ElasticSearchService;
import com.nisum.exception.custom.BadRequestException;
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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static com.nisum.util.Constants.NOT_FOUND;

@CrossOrigin("*")
@RestController
@RequestMapping(value = "/api/v1/elastic-search")
public class ElasticSearchController {

    private static final Logger LOGGER = LoggerFactory.getLogger(ElasticSearchController.class);
    @Autowired
    private ElasticSearchService elasticSearchService;

    @Operation(
            summary = "API for fetching all data in any ELK Index",
            description = "API for fetching all data in Elastic Search Index"
    )
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_VIEWER') ")
    @GetMapping(value = "/{indexName}")
    public ResponseEntity<Map<String, Object>> getData(@PathVariable String indexName) {
        LOGGER.info("Getting data based on index");
        try {
            List<ElasticCucumberJson> getData = elasticSearchService.getELKDataByIndex(indexName);
            return new EntityResponseSuccess<>(getData).getResponse();
        } catch (Exception ex) {
            LOGGER.info("Exception : {}", ex.getMessage());
            return new EntityResponseFailure(ex.getMessage()).getResponse();
        }
    }

    @Operation(
            summary = "API for fetching Project Summary Report",
            description = "API for fetching Project Summary Report data from Elastic Search Index"
    )
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_VIEWER') ")
    @GetMapping(value = "/project-summary-report")
    public ResponseEntity<Map<String, Object>> getSummaryReport(@RequestParam(name = "project") String project,
                                                                @RequestParam(name = "triggerDates", required = false) String triggerDates,
                                                                @RequestParam int page, @RequestParam int size) {
        LOGGER.info("Summary report getSummaryReport");
        try {
            List<String> buildDates = (triggerDates == null) ? new ArrayList<>() : Arrays.asList(triggerDates.split(","));
            return new EntityResponseSuccess<>(elasticSearchService.getProjectSummaryDetails(project, buildDates, page, size)).getResponse();
        } catch (BadRequestException ex) {
            LOGGER.error("Exception occurred on getting summary report {}", ex.getMessage());
            throw ex;
        } catch (Exception e) {
            LOGGER.error("Exception occurred on getting summary report {}", e.getMessage());
            return new EntityResponseFailure().getResponse();
        }
    }

    @Operation(
            summary = "API for fetching Detail Execution Report",
            description = "API for fetching Detail Execution Report data from Elastic Search Index"
    )
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_VIEWER') ")
    @GetMapping(value = "/detail-execution-report")
    public ResponseEntity<Map<String, Object>> getDetailExecutionReport(@RequestParam(name = "project") String project,
                                                                        @RequestParam(name = "triggerDates", required = false) String triggerDates) {
        LOGGER.info("Get Detail Execution Report");
        try {
            List<String> buildDates = (triggerDates == null) ? new ArrayList<>() : Arrays.asList(triggerDates.split(","));
            return new EntityResponseSuccess<>(elasticSearchService.getDetailExecutionReportData(project, buildDates)).getResponse();
        } catch (Exception e) {
            LOGGER.error("Exception occurred on getting detail execution report {}", e.getMessage());
            return new EntityResponseFailure().getResponse();
        }
    }

    @Operation(
            summary = "API for fetching all the counts for all scenarios",
            description = "API for fetching all the counts for all scenarios filtered on project, build and scenario"
    )
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_VIEWER') ")
    @GetMapping(value = "/scenarios-summary")
    public ResponseEntity<Map<String, Object>> getScenariosSummary(@RequestParam(name = "project") String project,
                                                                   @RequestParam(name = "triggerDates", required = false) String triggerDates,
                                                                   @RequestParam(name = "scenarioName", required = false) String scenarioName,
                                                                   @RequestParam int page, @RequestParam int size) {
        LOGGER.info("Get Scenarios Summary Counts from ELK");
        try {
            List<String> buildDates = (triggerDates == null) ? new ArrayList<>() : Arrays.asList(triggerDates.split(","));
            return new EntityResponseSuccess<>(elasticSearchService.getScenariosSummary(project, buildDates, scenarioName, page, size)).getResponse();
        } catch (BadRequestException ex) {
            LOGGER.error("Exception occurred on getting scenerios counts{}", ex.getMessage());
            throw ex;
        } catch (Exception e) {
            LOGGER.error("Exception occurred on getting scenarios' counts {}", e.getMessage());
            return new EntityResponseFailure().getResponse();
        }
    }

    @Operation(
            summary = "API for fetching all the scenarios",
            description = "API for fetching all the scenarios from Elastic Search"
    )
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_VIEWER') ")
    @GetMapping(value = "/scenarios")
    public ResponseEntity<Map<String, Object>> getScenarios(@RequestParam(name = "project", required = false) String project,
                                                            @RequestParam(name = "scenarioName", required = false) String scenarioName,
                                                            @RequestParam int page, @RequestParam int size) {
        LOGGER.info("Get all scenarios from ELK");
        try {
            return new EntityResponseSuccess<>(elasticSearchService.getScenarios(project, scenarioName, page, size)).getResponse();
        } catch (BadRequestException ex) {
            LOGGER.error("Exception occurred on getting all scenarios {}", ex.getMessage());
            throw ex;
        } catch (Exception e) {
            LOGGER.error("Exception occurred on getting all scenarios {}", e.getMessage());
            return new EntityResponseFailure().getResponse();
        }
    }

    @Operation(
            summary = "API for fetching all the scenarios for specific project",
            description = "API for fetching all the scenarios for specific project from Elastic Search"
    )
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_VIEWER') ")
    @GetMapping(value = "/scenarios-list")
    public ResponseEntity<Map<String, Object>> getProjectSpecificScenarios(@RequestParam(name = "project", required = false) String project) {
        LOGGER.info("Get all scenarios from ELK");
        try {
            return new EntityResponseSuccess<>(elasticSearchService.getProjectSpecificScenarios(project)).getResponse();
        } catch (Exception e) {
            LOGGER.error("Exception occurred on getting all scenarios for specific project {}", e.getMessage());
            return new EntityResponseFailure().getResponse();
        }
    }

    @Operation(
            summary = "API for fetching all the counts for all steps",
            description = "API for fetching all the counts for all steps filtered on project, build, scenario and step"
    )
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_VIEWER') ")
    @GetMapping(value = "/steps-summary")
    public ResponseEntity<Map<String, Object>> getScenariosSummary(@RequestParam(name = "project") String project,
                                                                   @RequestParam(name = "triggerDates", required = false) String triggerDates,
                                                                   @RequestParam(name = "scenarioName", required = false) String scenarioName,
                                                                   @RequestParam(name = "stepName", required = false) String stepName,
                                                                   @RequestParam int page,
                                                                   @RequestParam int size) {
        LOGGER.info("Get Steps Summary Counts from ELK");
        try {

            List<String> buildDates = (triggerDates == null) ? new ArrayList<>() : Arrays.asList(triggerDates.split(","));
            return new EntityResponseSuccess<>(elasticSearchService.getStepsSummary(project, buildDates, scenarioName, stepName, page, size)).getResponse();
        } catch (BadRequestException ex) {
            LOGGER.error("Exception occurred on getting steps summary{}", ex.getMessage());
            throw ex;
        } catch (Exception e) {
            LOGGER.error("Exception occurred on getting steps' counts {}", e.getMessage());
            return new EntityResponseFailure().getResponse();
        }
    }

    @Operation(
            summary = "API for fetching all the steps",
            description = "API for fetching all the steps from Elastic Search"
    )
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_VIEWER') ")
    @GetMapping(value = "/steps")
    public ResponseEntity<Map<String, Object>> getAllSteps(@RequestParam(name = "project", required = false) String project,
                                                           @RequestParam(name = "scenarioName", required = false) String scenarioName,
                                                           @RequestParam(name = "stepName", required = false) String stepName,
                                                           @RequestParam int page, @RequestParam int size) {
        LOGGER.info("Get All Steps from ELK");
        try {
            return new EntityResponseSuccess<>(elasticSearchService.getSteps(project, scenarioName, stepName, page, size)).getResponse();
        } catch (BadRequestException ex) {
            LOGGER.error("Exception occurred on getting all steps {}", ex.getMessage());
            throw ex;
        }catch (Exception e) {
            LOGGER.error("Exception occurred on getting all steps {}", e.getMessage());
            return new EntityResponseFailure().getResponse();
        }
    }

    @Operation(
            summary = "API for fetching all the steps for specific project",
            description = "API for fetching all the steps for specific project from Elastic Search"
    )
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_VIEWER') ")
    @GetMapping(value = "/steps-list")
    public ResponseEntity<Map<String, Object>> getProjectSpecificSteps(@RequestParam(name = "project", required = false) String project) {
        LOGGER.info("Get all steps from ELK");
        try {
            return new EntityResponseSuccess<>(elasticSearchService.getProjectSpecificSteps(project)).getResponse();
        } catch (Exception e) {
            LOGGER.error("Exception occurred on getting all steps for specific project {}", e.getMessage());
            return new EntityResponseFailure().getResponse();
        }
    }

    @Operation(
            summary = "API for fetching all the project with their builds",
            description = "API for fetching all the project with their builds from Elastic Search"
    )
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_VIEWER') ")
    @GetMapping(value = "/builds")
    public ResponseEntity<Map<String, Object>> getProjectBuilds() {
        LOGGER.info("Get all projects with their builds");
        try {
            ProjectsWithBuilds projectsWithBuilds = elasticSearchService.getProjectBuilds();
            return new EntityResponseSuccess<>(projectsWithBuilds).getResponse();
        } catch (WebClientResponseException e) {
            if (e.getClass().getSimpleName().equals(NOT_FOUND)) {
                return new EntityResponseFailure("Builds Index not found, please re-run the build", HttpStatus.BAD_REQUEST).getResponse();
            }
            return new EntityResponseFailure(e.getMessage(), HttpStatus.BAD_REQUEST).getResponse();
        } catch (Exception e) {
            LOGGER.error("Exception occurred getting projects with their builds {}", e.getMessage());
            return new EntityResponseFailure(e.getMessage(), HttpStatus.BAD_REQUEST).getResponse();
        }
    }
}
