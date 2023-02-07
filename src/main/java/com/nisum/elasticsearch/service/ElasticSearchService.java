package com.nisum.elasticsearch.service;

import com.nisum.elasticsearch.domain.DetailExecutionReport;
import com.nisum.elasticsearch.domain.ElasticCucumberJson;
import com.nisum.elasticsearch.domain.ProjectsWithBuilds;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface ElasticSearchService {
    List<ElasticCucumberJson> getELKDataByIndex(String indexName) throws Exception;

    ProjectsWithBuilds getProjectBuilds();

    Map<String, Boolean> insertData(MultipartFile file, String project, long buildDate, int buildNumber) throws Exception;

    Map<String, Object> getProjectSummaryDetails(String project, List<String> triggerDates, int page, int size);

    DetailExecutionReport getDetailExecutionReportData(String project, List<String> triggerDates);

    Map<String, Object> getScenariosSummary(String project, List<String> triggerDates, String scenarioName, int page, int size);

    Map<String, Object> getScenarios(String project, String scenarioName, int page, int size);

    Set<String> getProjectSpecificScenarios(String project);

    Map<String, Object> getStepsSummary(String project, List<String> triggerDates, String scenarioName, String stepName, int page, int size);

    Map<String, Object> getSteps(String project, String scenarioName, String stepName, int page, int size);

    Set<String> getProjectSpecificSteps(String project);

    String getFailedScenariosWithSteps(String project, List<String> triggerDates, String buildNumber);
}
