package com.nisum.elasticsearch.service;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.nisum.elasticsearch.domain.Computations;
import com.nisum.elasticsearch.domain.DetailExecutionReport;
import com.nisum.elasticsearch.domain.ElasticBeforeAfter;
import com.nisum.elasticsearch.domain.ElasticCucumberJson;
import com.nisum.elasticsearch.domain.ElasticCucumberResult;
import com.nisum.elasticsearch.domain.ElasticEmbeddings;
import com.nisum.elasticsearch.domain.Element;
import com.nisum.elasticsearch.domain.FailedScenarioWithSteps;
import com.nisum.elasticsearch.domain.FailedStep;
import com.nisum.elasticsearch.domain.Feature;
import com.nisum.elasticsearch.domain.FeatureData;
import com.nisum.elasticsearch.domain.LogHits;
import com.nisum.elasticsearch.domain.PassFailCounts;
import com.nisum.elasticsearch.domain.PassFailPercentage;
import com.nisum.elasticsearch.domain.ProjectScenariosCounts;
import com.nisum.elasticsearch.domain.ProjectStepsCounts;
import com.nisum.elasticsearch.domain.ProjectsWithBuilds;
import com.nisum.elasticsearch.domain.Scenario;
import com.nisum.elasticsearch.domain.ScenarioDTO;
import com.nisum.elasticsearch.domain.Status;
import com.nisum.elasticsearch.domain.Step;
import com.nisum.elasticsearch.domain.StepDTO;
import com.nisum.util.GenericUtils;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.client.indices.CreateIndexResponse;
import org.elasticsearch.client.indices.GetIndexRequest;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.rest.RestStatus;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.client.WebClient;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import static com.nisum.util.Constants.ELK_INDEX_FOR_PROJECTS;
import static com.nisum.util.GenericUtils.formatPercentage;
import static org.elasticsearch.index.query.QueryBuilders.boolQuery;

@Service
@Profile({"dev", "qa"})
public class ElasticSearchServiceImpl implements ElasticSearchService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ElasticSearchServiceImpl.class);

    @Autowired
    private WebClient webClient;

    @Value("${elastic.search.baseurl}")
    private String elasticSearchBaseUrl;

    @Value("${elastic.search.basehttp}")
    private String elasticSearchBaseHttp;

    @Value("${elastic.search.port}")
    private Integer elasticSearchBasePort;

    @Value("${elastic.search.username}")
    private String elasticSearchUsername;

    @Value("${elastic.search.password}")
    private String elasticSearchPassword;


    @Value("${elastic.index}")
    private String elasticIndex;

    @Value("${project.build.params}")
    private String projectBuildParams;


    private RestHighLevelClient getClient() throws Exception {
        if(elasticSearchUsername !=null && !elasticSearchUsername.trim().isEmpty()) {
            CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
            credentialsProvider.setCredentials(
                    AuthScope.ANY, new UsernamePasswordCredentials(elasticSearchUsername, elasticSearchPassword));

            RestClientBuilder builder =
                    RestClient.builder(new HttpHost(elasticSearchBaseUrl, elasticSearchBasePort, elasticSearchBaseHttp))
                            .setHttpClientConfigCallback(httpClientBuilder ->
                                    httpClientBuilder.setDefaultCredentialsProvider(credentialsProvider));
            return new RestHighLevelClient(builder);
        }
        else {
            RestHighLevelClient client = new RestHighLevelClient(
                    RestClient.builder(new HttpHost(elasticSearchBaseUrl, 9200, elasticSearchBaseHttp)));
            LOGGER.info("Client created ");
            return client;
        }
    }

    public List<ElasticCucumberJson> getELKDataByIndex(String indexName) throws Exception {

        if (!isIndexExist(indexName)) throw new Exception("Index not found.");

        LOGGER.info("Creating search request to fetch all data ");
        List<ElasticCucumberJson> modelList = mapSearchResponseToElasticCucumberJson(this.fetchDataFromElasticSearch(null, new ArrayList<>(), null, null));

        LOGGER.info("Response size is {} ", modelList.size());
        return modelList;
    }

    public ProjectsWithBuilds getProjectBuilds() {

        LOGGER.info("Collecting all project builds from elastic search");

        ProjectsWithBuilds projectsWithBuilds = new ProjectsWithBuilds();
        String allProjectsUrl = buildGetAllProjectsUrl();
        LOGGER.info("Fetching elastic search projects details with URL {}", allProjectsUrl);
        ElasticCucumberResult elasticCucumberResult;
        if(elasticSearchUsername !=null && !elasticSearchUsername.trim().isEmpty())
            elasticCucumberResult = webClient.get().uri(URI.create(allProjectsUrl))
                    .headers(headers -> headers.setBasicAuth(elasticSearchUsername,
                            elasticSearchPassword)).retrieve().bodyToMono(ElasticCucumberResult.class).
                    block();
        else
            elasticCucumberResult = webClient.get().uri(URI.create(allProjectsUrl)).retrieve().bodyToMono
                    (ElasticCucumberResult.class).block();
        LOGGER.info("Elastic search projects details fetching successful");


        List<LogHits> logHits = elasticCucumberResult.getHits().getHits();

        LOGGER.info("Total logHits retrieved from elastic search {}", logHits.size());

        for (LogHits hits : logHits) {
            Set<String> timestamps = null;
            String project = null;
            String triggerDate = null;
            project = hits.getSource().getProject();
            triggerDate = hits.getSource().getTriggerDate();
            if (project == null || triggerDate == null) continue;
            if (StringUtils.isEmpty(projectsWithBuilds.getBuilds().get(project))) {
                timestamps = new TreeSet<>();
            } else {
                timestamps = projectsWithBuilds.getBuilds().get(project);
            }
            timestamps.add(triggerDate);
            projectsWithBuilds.getBuilds().put(project.trim(), timestamps);
        }
        projectsWithBuilds.setProjects(projectsWithBuilds.getBuilds().keySet());

        for (Map.Entry<String, Set<String>> builds : projectsWithBuilds.getBuilds().entrySet()) {
            List<String> dates = new ArrayList<>(builds.getValue());
            Collections.sort(dates, new Comparator<String>() {
                DateFormat f = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");

                @Override
                public int compare(String o1, String o2) {
                    try {
                        return f.parse(o2).compareTo(f.parse(o1));
                    } catch (ParseException e) {
                        throw new IllegalArgumentException(e);
                    }
                }
            });
            projectsWithBuilds.getBuilds().put(builds.getKey(), new HashSet<>(dates));
        }
        return projectsWithBuilds;
    }


    public Map<String, Boolean> insertData(MultipartFile file, String project, long buildDate, int buildNumber) throws Exception {
        Map<String, Boolean> responseMap = new HashMap<>();
        try {
            List<FeatureData> featureDataList = convertJsonFileToObjects(file);
            if (!featureDataList.isEmpty()) {
                LOGGER.info("Checking index created");
                if (isIndexCreated(GenericUtils.formatIndex(project))) {
                    ElasticCucumberJson elasticCucumberJson = new ElasticCucumberJson();
                    elasticCucumberJson.setData(featureDataList);
                    elasticCucumberJson.setProject(project);
                    elasticCucumberJson.setTriggerDate(GenericUtils.formatDateForELK(buildDate));
                    elasticCucumberJson.setBuildNumber(buildNumber);

                    if (isIndexCreated(ELK_INDEX_FOR_PROJECTS)) {
                        LOGGER.info("Creating bulk request for all projects");
                        BulkRequest bulkRequest = new BulkRequest();
                        IndexRequest indexRequest = new IndexRequest(ELK_INDEX_FOR_PROJECTS);
                        indexRequest.source(ElasticCucumberJson.getAsMap(elasticCucumberJson));
                        bulkRequest.add(indexRequest);

                        BulkResponse bulkResponses = getClient().bulk(bulkRequest, RequestOptions.DEFAULT);
                        LOGGER.info("Response status after saving data in main index : " +
                                bulkResponses.status().getStatus());
                    }
                    LOGGER.info("Creating bulk request for build data");
                    BulkRequest bulkRequest = new BulkRequest();
                    IndexRequest indexRequest = new IndexRequest(GenericUtils.formatIndex(project));
                    indexRequest.source(ElasticCucumberJson.getAsMap(elasticCucumberJson));
                    bulkRequest.add(indexRequest);

                    BulkResponse bulkResponses = getClient().bulk(bulkRequest, RequestOptions.DEFAULT);
                    LOGGER.info("Response status after saving data in project specific index: {} ",
                            bulkResponses.status().getStatus());


                    responseMap.put("isFailedScenariosExist", isFailScenarioExist(featureDataList));

                    if (bulkResponses.status() == RestStatus.OK) {
                        responseMap.put("isInserted", Boolean.TRUE);
                    } else {
                        responseMap.put("isInserted", Boolean.FALSE);
                    }
                    return responseMap;
                }
            }
        } catch (java.net.ConnectException e) {
            LOGGER.info("Connect Exception : ".concat(e.getMessage()));
            throw new RuntimeException(e.getLocalizedMessage());
        } catch (Exception ex) {
            LOGGER.info("Exception : {} ", ex.getMessage());
            throw new Exception(ex.getLocalizedMessage());
        }
        responseMap.put("isInserted", Boolean.FALSE);
        responseMap.put("isFailedScenariosExist", Boolean.FALSE);
        return responseMap;
    }

    private Boolean isFailScenarioExist(List<FeatureData> featureDataList) {
        for (FeatureData featureData : featureDataList){
            for(Element element: featureData.getElements()){
                for (Step step : element.getSteps()) {
                    if(step.getResult().getStatus().toLowerCase().contains("fail")){
                        LOGGER.info("Fail Scenario exist in File");
                        return Boolean.TRUE;
                    }
                }
            }
        }
        return Boolean.FALSE;
    }

    private boolean isIndexCreated(String index) throws Exception {
        boolean isExist = isIndexExist(index);
        boolean isCreated = true;
        LOGGER.info("Checking index exist: {}", index);
        if (!isExist) {
            isCreated = createIndex(index);
        }
        return isCreated;
    }

    private boolean isIndexExist(String indexName) throws Exception {
        LOGGER.info("Finding index : " + indexName);
        try {
            GetIndexRequest getRequest = new GetIndexRequest(indexName);

            return getClient().indices().exists(getRequest, RequestOptions.DEFAULT);

        } catch (Exception ex) {
            LOGGER.info("Exception : " + ex.getMessage());
            ex.printStackTrace();
            throw ex;
        }
    }

    private boolean createIndex(String indexName) throws Exception {
        LOGGER.info("Creating new index with name :" + indexName);
        try {
            CreateIndexRequest request = new CreateIndexRequest(indexName);
            request.settings(Settings.builder().put("index.number_of_shards", 1).put("index.number_of_replicas", 2).put("max_result_window", 1000));
            CreateIndexResponse createIndexResponse = getClient().indices().create(request, RequestOptions.DEFAULT);
            LOGGER.info("Response id: " + createIndexResponse.index());
            return true;
        } catch (Exception ex) {
            LOGGER.info("Exception :" + ex.getMessage());
            ex.printStackTrace();
            throw ex;
        }
    }

    private List<FeatureData> convertJsonFileToObjects(MultipartFile file) {
        try {
            List<FeatureData> featureDataList = new ArrayList<>();
            LOGGER.info("Converting data from file to java object");
            String s = new String(file.getBytes(), StandardCharsets.UTF_8);
            Gson gson = new Gson();
            JsonParser parser = new JsonParser();
            JsonArray jsonElements = (JsonArray) parser.parse(s);// response will be the json String
            for (int i = 0; i < jsonElements.size(); i++) {
                JsonElement jsonElement = jsonElements.get(i);
                featureDataList.add(gson.fromJson(jsonElement, FeatureData.class).format());
            }
            LOGGER.info("Successfully converted cucumber file to java object");
            return featureDataList;

        } catch (Exception ex) {
            LOGGER.info("Exception when converting cucumber json file to json: " + ex.getMessage());
            return new ArrayList<>();
        }
    }

    private List<ElasticCucumberJson> mapSearchResponseToElasticCucumberJson(SearchResponse searchResponse) {
        LOGGER.info("Converting Search Response to Search Response DTO");
        Gson gson = new Gson();

        List<ElasticCucumberJson> modelList = new ArrayList<>();
        if (searchResponse.getHits().getTotalHits().value > 0) {
            SearchHit[] searchHit = searchResponse.getHits().getHits();
            Map<String, Object> map;
            for (SearchHit hit : searchHit) {
                map = hit.getSourceAsMap();
                JsonElement element = gson.toJsonTree(map);
                ElasticCucumberJson model = gson.fromJson(element, ElasticCucumberJson.class);
                modelList.add(model);
            }
            LOGGER.info("Successfully converted to DTO from JSON");
        }
        return modelList;
    }

    public Map<String, Object> getProjectSummaryDetails(String project, List<String> triggerDates, int page, int size) {
        LOGGER.info("Generating project summary details");

        List<ProjectScenariosCounts> projectScenariosCountsList = new ArrayList<>();

        SearchResponse searchResponse = this.fetchDataFromElasticSearch(project, triggerDates, null, null);
        SearchHit[] searchHits = searchResponse.getHits().getHits();
        LOGGER.info("Total searchHits retrieved from elastic search {}", searchResponse.getHits().getTotalHits().value);

        for (SearchHit hit : searchHits) {
            Gson gson = new Gson();
            ElasticCucumberJson elasticCucumberJson = gson.fromJson(gson.toJsonTree(hit.getSourceAsMap()), ElasticCucumberJson.class);
            List<FeatureData> featureDataList = elasticCucumberJson.getData();
            LOGGER.info("Summary Report Counts for Project: {} with Build: {} ", elasticCucumberJson.getProject(), elasticCucumberJson.getTriggerDate());
            int failedScenarios = 0;
            int passedScenarios = 0;
            int skippedScenarios = 0;
            for (FeatureData featureData : featureDataList) {
                LOGGER.info("Calculating scenarios pass fail counts for Summary Report...");

                for (Element element : featureData.getElements()) {
                    PassFailCounts passFailCounts = this.calculateStepCounts(element);
                    if (passFailCounts.getFailedCount() == 0 && passFailCounts.getSkippedCount() == 0) {
                        passedScenarios++;
                    } else if (passFailCounts.getFailedCount() == 0 && passFailCounts.getPassedCount() == 0) {
                        skippedScenarios++;
                    } else {
                        failedScenarios++;
                    }
                }
            }

            int totalScenariosCount = failedScenarios + passedScenarios + skippedScenarios;
            ProjectScenariosCounts projectScenariosCounts = new ProjectScenariosCounts();
            projectScenariosCounts.setProjectName(project);
            projectScenariosCounts.setBuildNumber(elasticCucumberJson.getBuildNumber());
            projectScenariosCounts.setBuildDate(elasticCucumberJson.getTriggerDate());
            projectScenariosCounts.setFailedScenariosCount(failedScenarios);
            projectScenariosCounts.setPassedScenariosCount(passedScenarios);
            projectScenariosCounts.setSkippedScenariosCount(skippedScenarios);
            projectScenariosCounts.setTotalScenariosCount(totalScenariosCount);
            projectScenariosCounts.setPassedScenariosPercentage(passedScenarios == 0 ? 0 : formatPercentage(100 * passedScenarios / totalScenariosCount));
            projectScenariosCounts.setFailedScenariosPercentage(failedScenarios == 0 ? 0 : formatPercentage(100 * failedScenarios / totalScenariosCount));
            projectScenariosCounts.setSkippedScenariosPercentage(skippedScenarios == 0 ? 0 : formatPercentage(100 * skippedScenarios / totalScenariosCount));
            projectScenariosCountsList.add(projectScenariosCounts);

        }
        LOGGER.info("Project Summary Counts Calculated Successfully");
        //Custom Pagination
        return GenericUtils.getPaginatedResponse(GenericUtils.getPages(projectScenariosCountsList, page, size), page, size, projectScenariosCountsList.size());
    }

    public DetailExecutionReport getDetailExecutionReportData(String project, List<String> triggerDates) {
        LOGGER.info("Generating Detail Execution Report Data");

        DetailExecutionReport detailExecutionReport = new DetailExecutionReport();
        PassFailPercentage featuresSummary = new PassFailPercentage();
        PassFailPercentage scenariosSummary = new PassFailPercentage();
        PassFailPercentage stepsSummary = new PassFailPercentage();

        List<Feature> features = new ArrayList<>();
        Computations computations = new Computations();

        SearchResponse searchResponse = this.fetchDataFromElasticSearch(project, triggerDates, null, null);
        SearchHit[] searchHits = searchResponse.getHits().getHits();
        LOGGER.info("Total searchHits retrieved from elastic search : {}", searchResponse.getHits().getTotalHits().value);

        for (SearchHit hit : searchHits) {
            Gson gson = new Gson();
            ElasticCucumberJson elasticCucumberJson = gson.fromJson(gson.toJsonTree(hit.getSourceAsMap()), ElasticCucumberJson.class);
            List<FeatureData> featureDataList = elasticCucumberJson.getData();
            LOGGER.info("Detail Execution Report Counts for Project: {} with Build: {} ", elasticCucumberJson.getProject(), elasticCucumberJson.getTriggerDate());
            int failedFeaturesCount = 0;
            int passedFeaturesCount = 0;
            int skippedFeaturesCount = 0;
            for (FeatureData featureData : featureDataList) {
                LOGGER.info("Calculating scenarios pass fail counts for Detail Execution Report...");
                int failedScenarios = 0;
                int passedScenarios = 0;
                int skippedScenarios = 0;
                Feature feature = new Feature();
                feature.setFeatureName(featureData.getFeatureName());

                List<Scenario> scenarios = new ArrayList<>();
                for (Element element : featureData.getElements()) {
                    LOGGER.info("Calculating steps pass fail counts for Detail Execution Report...");
                    Scenario scenario = new Scenario();
                    int passed = 0;
                    int failed = 0;
                    int skipped = 0;
                    List<StepDTO> steps = new ArrayList<>();
                    for (Step step : element.getSteps()) {
                        if (step.getResult().getStatus().equals(Status.Passed.value)) {
                            passed++;
                        } else if (step.getResult().getStatus().equals(Status.Failed.value)) {
                            failed++;
                        } else {
                            skipped++;
                        }

                        StepDTO stepDTO = new StepDTO();
                        stepDTO.setStepName(step.getStepName());
                        stepDTO.setStatus(Status.getName(step.getResult().getStatus()));
                        stepDTO.setDuration(GenericUtils.convertToSeconds(step.getResult().getDuration()));
                        steps.add(stepDTO);
                    }
                    scenario.setScenarioName(element.getScenarioName());
                    scenario.setFailedStepsCount(failed);
                    scenario.setPassedStepsCount(passed);
                    scenario.setSkippedStepsCount(skipped);
                    scenario.setSteps(steps);

                    if (element.getAfter() != null) {
                        for (ElasticBeforeAfter elasticBeforeAfter : element.getAfter()) {
                            if (elasticBeforeAfter.getEmbeddings() != null) {
                                for (ElasticEmbeddings embeddings : elasticBeforeAfter.getEmbeddings()) {
                                    LOGGER.info("Adding Snapshot if scenario failed...");
                                    scenario.getImages().add(embeddings.getData());
                                }
                            }
                        }
                    }

                    computations.setStepsSummary(stepsSummary.addValues(failed, passed, skipped));
                    scenarios.add(scenario);

                    if (failed == 0 && skipped == 0) {
                        passedScenarios++;
                    } else if (failed == 0 && passed == 0) {
                        skippedScenarios++;
                    } else {
                        failedScenarios++;
                    }
                }
                feature.setScenarios(scenarios);
                feature.setPassedScenariosCount(passedScenarios);
                feature.setFailedScenariosCount(failedScenarios);
                feature.setSkippedScenariosCount(skippedScenarios);
                computations.setScenariosSummary(scenariosSummary.addValues(failedScenarios, passedScenarios, skippedScenarios));
                features.add(feature);

                if (failedScenarios == 0 && skippedScenarios == 0) {
                    passedFeaturesCount++;
                } else if (failedScenarios == 0 && passedScenarios == 0) {
                    skippedFeaturesCount++;
                } else {
                    failedFeaturesCount++;
                }
                LOGGER.info("Enriching scenarios with steps count successful ");
            }

            detailExecutionReport.setPassedFeaturesCount(passedFeaturesCount);
            detailExecutionReport.setFailedFeaturesCount(failedFeaturesCount);
            detailExecutionReport.setSkippedFeaturesCount(skippedFeaturesCount);

            computations.setFeaturesSummary(featuresSummary.addValues(failedFeaturesCount, passedFeaturesCount, skippedFeaturesCount));
        }
        detailExecutionReport.setComputations(computations);
        detailExecutionReport.setFeatures(features);

        LOGGER.info("Elastic cucumber result enriched with build counts");
        return detailExecutionReport;

    }

    public Map<String, Object> getScenariosSummary(String project, List<String> triggerDates, String scenarioName, int page, int size) {
        LOGGER.info("Generating scenario summary details");

        HashMap<String, ProjectScenariosCounts> scenariosCountsHashMap = new HashMap<>();

        SearchResponse searchResponse = this.fetchDataFromElasticSearch(project, triggerDates, scenarioName, null);
        SearchHit[] searchHits = searchResponse.getHits().getHits();
        LOGGER.info("Total searchHits retrieved from elastic search : {}", searchResponse.getHits().getTotalHits().value);

        for (SearchHit hit : searchHits) {
            Gson gson = new Gson();
            ElasticCucumberJson elasticCucumberJson = gson.fromJson(gson.toJsonTree(hit.getSourceAsMap()), ElasticCucumberJson.class);
            List<FeatureData> featureDataList = elasticCucumberJson.getData();
            LOGGER.info("Scenarios Report Counts for Project: {} with Build: {} ", elasticCucumberJson.getProject(), elasticCucumberJson.getTriggerDate());
            for (FeatureData featureData : featureDataList) {
                LOGGER.info("Calculating scenarios pass fail counts for Scenarios Report...");
                for (Element element : featureData.getElements()) {
                    if (scenarioName != null && !element.getScenarioName().equals(scenarioName)) {
                        continue;   // Skip the scenario if it doesn't match given scenario in params
                    }
                    PassFailCounts passFailCounts = this.calculateStepCounts(element);
                    if (passFailCounts.getFailedCount() == 0 && passFailCounts.getSkippedCount() == 0) {
                        this.addCountsToScenariosCountsMap(scenariosCountsHashMap, elasticCucumberJson, element.getScenarioName(), 1, 0, 0);
                    } else if (passFailCounts.getFailedCount() == 0 && passFailCounts.getPassedCount() == 0) {
                        this.addCountsToScenariosCountsMap(scenariosCountsHashMap, elasticCucumberJson, element.getScenarioName(), 0, 0, 1);
                    } else {
                        this.addCountsToScenariosCountsMap(scenariosCountsHashMap, elasticCucumberJson, element.getScenarioName(), 0, 1, 0);
                    }
                }
            }
        }
        List<ProjectScenariosCounts> projectScenariosCountsList = scenariosCountsHashMap.entrySet().stream().map(x -> x.getValue()).collect(Collectors.toList());
        LOGGER.info("Scenarios Counts Calculated Successfully");
        // Custom Pagination
        return GenericUtils.getPaginatedResponse(GenericUtils.getPages(projectScenariosCountsList, page, size), page, size, projectScenariosCountsList.size());
    }

    public Map<String, Object> getScenarios(String project, String scenarioName, int page, int size) {
        LOGGER.info("Getting All Scenarios via Elastic Search");

        List<ScenarioDTO> scenarios = new ArrayList<>();

        SearchResponse searchResponse = this.fetchDataFromElasticSearch(project, new ArrayList<>(), scenarioName, null);
        SearchHit[] searchHits = searchResponse.getHits().getHits();
        LOGGER.info("Total searchHits retrieved from elastic search : {}", searchResponse.getHits().getTotalHits().value);

        for (SearchHit hit : searchHits) {
            Gson gson = new Gson();
            ElasticCucumberJson elasticCucumberJson = gson.fromJson(gson.toJsonTree(hit.getSourceAsMap()), ElasticCucumberJson.class);
            List<FeatureData> featureDataList = elasticCucumberJson.getData();
            for (FeatureData featureData : featureDataList) {
                for (Element element : featureData.getElements()) {
                    if (scenarioName != null && !element.getScenarioName().equals(scenarioName)) {
                        continue;   // Skip the scenario if it doesn't match given scenario in params
                    }
                    ScenarioDTO scenario = new ScenarioDTO();
                    PassFailCounts passFailCounts = this.calculateStepCounts(element);
                    if (passFailCounts.getFailedCount() == 0 && passFailCounts.getSkippedCount() == 0) {
                        scenario.setStatus(Status.Passed.name());
                    } else if (passFailCounts.getFailedCount() == 0 && passFailCounts.getPassedCount() == 0) {
                        scenario.setStatus(Status.Skipped.name());
                    } else {
                        scenario.setStatus(Status.Failed.name());
                    }
                    scenario.setScenarioName(element.getScenarioName());
                    scenario.setTimestamp(element.getScenarioTimestamp());
                    scenario.setBuildNumber(elasticCucumberJson.getBuildNumber());
                    scenario.setProject(elasticCucumberJson.getProject());
                    scenarios.add(scenario);
                }
            }
        }
        LOGGER.info("Scenarios List Successfully Fetched");
        // Custom Pagination
        return GenericUtils.getPaginatedResponse(GenericUtils.getPages(scenarios, page, size), page, size, scenarios.size());
    }

    public Set<String> getProjectSpecificScenarios(String project) {
        LOGGER.info("Getting All Scenarios via Elastic Search for Project {} ", project);

        Set<String> allScenarios = new HashSet<>();

        SearchResponse searchResponse = this.fetchDataFromElasticSearch(project, new ArrayList<>(), null, null);
        SearchHit[] searchHits = searchResponse.getHits().getHits();
        LOGGER.info("Total searchHits retrieved from elastic search : {}", searchResponse.getHits().getTotalHits().value);

        for (SearchHit hit : searchHits) {
            Gson gson = new Gson();
            ElasticCucumberJson elasticCucumberJson = gson.fromJson(gson.toJsonTree(hit.getSourceAsMap()), ElasticCucumberJson.class);
            List<FeatureData> featureDataList = elasticCucumberJson.getData();
            for (FeatureData featureData : featureDataList) {
                for (Element element : featureData.getElements()) {
                    allScenarios.add(element.getScenarioName());
                }
            }
        }
        LOGGER.info("Scenarios List Successfully Fetched");
        return allScenarios;
    }

    public Map<String, Object> getStepsSummary(String project, List<String> triggerDates, String scenarioName, String stepName, int page, int size) {
        LOGGER.info("Generating scenario summary details");

        List<ProjectStepsCounts> stepsCountsList = new ArrayList<>();
        HashMap<String, ProjectStepsCounts> stepsCountsMap = new HashMap<>();

        SearchResponse searchResponse = this.fetchDataFromElasticSearch(project, triggerDates, scenarioName, stepName);
        SearchHit[] searchHits = searchResponse.getHits().getHits();
        LOGGER.info("Total searchHits retrieved from elastic search {}", searchResponse.getHits().getTotalHits().value);

        for (SearchHit hit : searchHits) {
            Map<String, Object> map = hit.getSourceAsMap();
            Gson gson = new Gson();
            ElasticCucumberJson elasticCucumberJson = gson.fromJson(gson.toJsonTree(map), ElasticCucumberJson.class);
            List<FeatureData> featureDataList = elasticCucumberJson.getData();
            LOGGER.info("Steps Report Counts for Project: {} with Build: {} ", elasticCucumberJson.getProject(), elasticCucumberJson.getTriggerDate());
            for (FeatureData featureData : featureDataList) {
                LOGGER.info("Calculating scenarios pass fail counts for Steps Report...");
                for (Element element : featureData.getElements()) {
                    if (scenarioName != null && !element.getScenarioName().equals(scenarioName)) {
                        continue;   // Skip the scenario if it doesn't match given scenario in params
                    }
                    for (Step step : element.getSteps()) {
                        if (stepName != null && !step.getStepName().equals(stepName)) {
                            continue;   // Skip the step if it doesn't match given scenario in params
                        }
                        if (step.getResult().getStatus().equals(Status.Passed.value)) {
                            this.addCountsToStepsCountsMap(stepsCountsMap, element.getScenarioName(), step.getStepName(), 1, 0, 0);

                        } else if (step.getResult().getStatus().equals(Status.Failed.value)) {
                            this.addCountsToStepsCountsMap(stepsCountsMap, element.getScenarioName(), step.getStepName(), 0, 1, 0);
                        } else {
                            this.addCountsToStepsCountsMap(stepsCountsMap, element.getScenarioName(), step.getStepName(), 0, 0, 1);
                        }
                        ProjectStepsCounts projectStepsCountMap = stepsCountsMap.get(step.getStepName());
                        projectStepsCountMap.setBuildNumber(elasticCucumberJson.getBuildNumber());
                        projectStepsCountMap.setStatus(Status.getName(step.getResult().getStatus()));
                        projectStepsCountMap.setDuration(GenericUtils.convertToSeconds(step.getResult().getDuration()));
                    }
                }
            }
            LOGGER.info("Steps Counts Calculated Successfully");
            LOGGER.info("Converting Steps Counts Map to List");
            stepsCountsList = stepsCountsMap.entrySet().stream().map(x -> x.getValue()).collect(Collectors.toList());
        }
        // Custom Pagination
        return GenericUtils.getPaginatedResponse(GenericUtils.getPages(stepsCountsList, page, size), page, size, stepsCountsList.size());
    }

    public Map<String, Object> getSteps(String project, String scenarioName, String stepName, int page, int size) {
        LOGGER.info("Getting All Steps via Elastic Search");

        List<StepDTO> steps = new ArrayList<>();

        SearchResponse searchResponse = this.fetchDataFromElasticSearch(project, new ArrayList<>(), scenarioName, stepName);
        SearchHit[] searchHits = searchResponse.getHits().getHits();
        LOGGER.info("Total searchHits retrieved from elastic search : {}", searchResponse.getHits().getTotalHits().value);

        for (SearchHit hit : searchHits) {
            Gson gson = new Gson();
            ElasticCucumberJson elasticCucumberJson = gson.fromJson(gson.toJsonTree(hit.getSourceAsMap()), ElasticCucumberJson.class);
            List<FeatureData> featureDataList = elasticCucumberJson.getData();
            for (FeatureData featureData : featureDataList) {
                for (Element element : featureData.getElements()) {
                    if (scenarioName != null && !element.getScenarioName().equals(scenarioName)) {
                        continue;   // Skip the scenario if it doesn't match given scenario in params
                    }
                    for (Step step : element.getSteps()) {
                        if (stepName != null && !step.getStepName().equals(stepName)) {
                            continue;   // Skip the step if it doesn't match given scenario in params
                        }
                        StepDTO stepDTO = new StepDTO();
                        stepDTO.setScenarioName(element.getScenarioName());
                        stepDTO.setStepName(step.getStepName());
                        stepDTO.setDuration(GenericUtils.convertToSeconds(step.getResult().getDuration()));
                        stepDTO.setStatus(Status.getName(step.getResult().getStatus()));
                        stepDTO.setBuildNumber(elasticCucumberJson.getBuildNumber());
                        stepDTO.setProject(elasticCucumberJson.getProject());
                        steps.add(stepDTO);
                    }
                }
            }
        }
        LOGGER.info("Steps List Successfully Fetched");
        // Custom Pagination
        return GenericUtils.getPaginatedResponse(GenericUtils.getPages(steps, page, size), page, size, steps.size());
    }

    public Set<String> getProjectSpecificSteps(String project) {
        LOGGER.info("Getting All Steps via Elastic Search for Project {} ", project);

        Set<String> allSteps = new HashSet<>();

        SearchResponse searchResponse = this.fetchDataFromElasticSearch(project, new ArrayList<>(), null, null);
        SearchHit[] searchHits = searchResponse.getHits().getHits();
        LOGGER.info("Total searchHits retrieved from elastic search : {}", searchResponse.getHits().getTotalHits().value);

        for (SearchHit hit : searchHits) {
            Gson gson = new Gson();
            ElasticCucumberJson elasticCucumberJson = gson.fromJson(gson.toJsonTree(hit.getSourceAsMap()), ElasticCucumberJson.class);
            List<FeatureData> featureDataList = elasticCucumberJson.getData();
            for (FeatureData featureData : featureDataList) {
                for (Element element : featureData.getElements()) {
                    for (Step step : element.getSteps()) {
                        allSteps.add(step.getStepName());
                    }
                }
            }
        }
        LOGGER.info("Steps List Successfully Fetched for Project");
        return allSteps;
    }

    public String getFailedScenariosWithSteps(String project, List<String> triggerDates, String buildNumber) {

        SearchResponse searchResponse = this.fetchDataFromElasticSearch(project, triggerDates, null, null);
        SearchHit[] searchHits = searchResponse.getHits().getHits();
        LOGGER.info("Total searchHits retrieved from elastic search : {}", searchResponse.getHits().getTotalHits().value);

        String failedTests = "";
        for (SearchHit hit : searchHits) {
            Gson gson = new Gson();
            ElasticCucumberJson elasticCucumberJson = gson.fromJson(gson.toJsonTree(hit.getSourceAsMap()), ElasticCucumberJson.class);
            if (buildNumber.equals(String.valueOf(elasticCucumberJson.getBuildNumber()))) {
                List<FeatureData> featureDataList = elasticCucumberJson.getData();
                LOGGER.info("Failed Scenarios With Steps for Project: {} with Build: {} ", elasticCucumberJson.getProject(), elasticCucumberJson.getTriggerDate());
                for (FeatureData featureData : featureDataList) {
                    LOGGER.info("Calculating scenarios pass fail counts for Jira Creation...");
                    for (Element element : featureData.getElements()) {
                        LOGGER.info("Calculating steps pass fail counts for Jira Creation...");
                        int passed = 0;
                        int failed = 0;
                        int skipped = 0;
                        List<FailedStep> failedSteps = new ArrayList<>();
                        for (Step step : element.getSteps()) {
                            if (step.getResult().getStatus().equals(Status.Passed.value)) {
                                passed++;
                            } else if (step.getResult().getStatus().equals(Status.Failed.value)) {
                                failed++;
                            } else {
                                skipped++;
                            }

                            FailedStep failedStep = new FailedStep();
                            failedStep.setStep(step.getStepName());
                            failedStep.setStatus(GenericUtils.convertToSeconds(step.getResult().getDuration()));
                            failedSteps.add(failedStep);
                        }

                        if (!(failed == 0 && skipped == 0) && !(failed == 0 && passed == 0)) {
                            FailedScenarioWithSteps scenario = new FailedScenarioWithSteps();
                            scenario.setScenario(element.getScenarioName());
                            scenario.setSteps(failedSteps);
                            failedTests += scenario.toString();
                        }
                    }

                    LOGGER.info("Enriching scenarios with steps count successful ");
                }
            }
        }
        LOGGER.info("Elastic cucumber result enriched with build counts");
        return failedTests;
    }

    private SearchResponse fetchDataFromElasticSearch(String project, List<String> triggerDates, String scenarioName, String stepName) {
        try {
            SearchRequest searchRequest = new SearchRequest();
            searchRequest.indices(GenericUtils.formatIndex(project != null ? project : ELK_INDEX_FOR_PROJECTS));

            SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();

            BoolQueryBuilder filterQuery = boolQuery();

            if (!triggerDates.isEmpty()) {
                int index = 0;
                for (String triggerDate : triggerDates) {
                    triggerDates.set(index++, triggerDate.replace(" ", "+"));
                }
                filterQuery.filter(QueryBuilders.termsQuery("triggerDate", triggerDates));
            }


            filterQuery = (scenarioName != null) ? filterQuery.must(QueryBuilders.matchPhraseQuery("data.elements.scenarioName", scenarioName)) : filterQuery;
            filterQuery = (stepName != null) ? filterQuery.must(QueryBuilders.matchPhraseQuery("data.elements.steps.stepName", stepName)) : filterQuery;
            if (filterQuery != null) {
                sourceBuilder.query(filterQuery);
                sourceBuilder.size(1000);
            }

            searchRequest.source(sourceBuilder);
            LOGGER.info("Search Index : {} & Query : {}", searchRequest.indices(), searchRequest.source());
            LOGGER.info("Elastic cucumber result fetching successful");
            return getClient().search(searchRequest, RequestOptions.DEFAULT);
        } catch (Exception e) {
            LOGGER.error("Exception : ", e);
            throw new RuntimeException(e.getLocalizedMessage());
        }

    }

    private String buildGetAllProjectsUrl() {
        return new StringBuilder(buildIndexUrl(ELK_INDEX_FOR_PROJECTS)).append(projectBuildParams).toString();
    }

    private String buildIndexUrl(String project) {
        return new StringBuilder(getElasticSearchBaseUrl()).append(elasticIndex.replace("$index", GenericUtils.formatIndex(project))).toString();
    }

    private String getElasticSearchBaseUrl() {

        return new StringBuilder(elasticSearchBaseHttp).append("://").append(elasticSearchBaseUrl).append(":").append(elasticSearchBasePort).toString();
    }

    private HashMap<String, ProjectScenariosCounts> addCountsToScenariosCountsMap(HashMap<String, ProjectScenariosCounts> countsHashMap, ElasticCucumberJson json, String scenarioName, int passed, int failed, int skipped) {
        ProjectScenariosCounts projectScenariosCounts = countsHashMap.get(scenarioName);
        if (projectScenariosCounts != null) {
            projectScenariosCounts.mapValues(json.getProject(), json.getBuildNumber(), json.getTriggerDate(), scenarioName, (projectScenariosCounts.getFailedScenariosCount() + failed), (projectScenariosCounts.getPassedScenariosCount() + passed), (projectScenariosCounts.getSkippedScenariosCount() + skipped));
        } else {
            projectScenariosCounts = new ProjectScenariosCounts();
            projectScenariosCounts.mapValues(json.getProject(), json.getBuildNumber(), json.getTriggerDate(), scenarioName, failed, passed, skipped);
        }
        countsHashMap.put(scenarioName, projectScenariosCounts);
        return countsHashMap;
    }

    private HashMap<String, ProjectStepsCounts> addCountsToStepsCountsMap(HashMap<String, ProjectStepsCounts> countsHashMap, String scenarioName, String stepName, int passed, int failed, int skipped) {
        ProjectStepsCounts projectStepsCounts = countsHashMap.get(stepName);
        if (projectStepsCounts != null) {
            projectStepsCounts.mapValues(scenarioName, stepName, (projectStepsCounts.getFailedStepsCount() + failed), (projectStepsCounts.getPassedStepsCount() + passed), (projectStepsCounts.getSkippedStepsCount() + skipped));
        } else {
            projectStepsCounts = new ProjectStepsCounts();
            projectStepsCounts.mapValues(scenarioName, stepName, failed, passed, skipped);
        }
        countsHashMap.put(stepName, projectStepsCounts);
        return countsHashMap;
    }

    private PassFailCounts calculateStepCounts(Element element) {
        PassFailCounts passFailCounts = new PassFailCounts();
        int passed = 0;
        int failed = 0;
        int skipped = 0;
        LOGGER.info("Calculating steps pass fail counts...");
        for (Step step : element.getSteps()) {
            if (step.getResult().getStatus().equals(Status.Passed.value)) {
                passed++;
            } else if (step.getResult().getStatus().equals(Status.Failed.value)) {
                failed++;
            } else {
                skipped++;
            }
        }
        return passFailCounts.mapValues(failed, passed, skipped);
    }
}
