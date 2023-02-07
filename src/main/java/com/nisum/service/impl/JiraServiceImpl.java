package com.nisum.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nisum.api.preset.domain.entity.JiraPreset;
import com.nisum.jira.domain.dto.TicketDetail;
import com.nisum.service.JiraService;
import com.nisum.util.FileUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.tomcat.util.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
@Slf4j
@Profile({"dev", "qa"})
public class JiraServiceImpl implements JiraService {
    private static final Logger LOGGER = LoggerFactory.getLogger(JiraServiceImpl.class);

    @Autowired
    RestTemplate restTemplate;

    @Value("${jira.post-ticket.url}")
    private String postTicketUrl;

    @Value("${jira.add-attachment.url}")
    private String addAttachmentUrl;

    @Value("${jira.add-comment.url}")
    private String addCommentUrl;

    @Value("${jira.get-tickets-by-summary.url}")
    private String getTicketsBySummaryURL;

    public HttpHeaders getHttpHeaders(JiraPreset jiraPreset) {
        HttpHeaders headers = new HttpHeaders();
        String plainCreds = jiraPreset.getUserName() + ":" + jiraPreset.getToken();
        byte[] plainCredsBytes = plainCreds.getBytes();
        byte[] base64CredsBytes = Base64.encodeBase64(plainCredsBytes);
        // set `Content-Type` and `Accept` headers
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        headers.add("Authorization", "Basic " + new String(base64CredsBytes));
        return headers;
    }

    private MultiValueMap<String, Object> getFileMultiValueMap(String fileName) throws IOException {
        MultiValueMap<String, String> fileMap = new LinkedMultiValueMap<>();
        fileMap.add(HttpHeaders.CONTENT_DISPOSITION, FileUtils.getFileContentDisposition(fileName));
        HttpEntity<byte[]> fileEntity = new HttpEntity<>(FileUtils.getFileContentInBytes(fileName), fileMap);
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", fileEntity);
        return body;
    }

    public TicketDetail createJiraTicket(TicketDetail ticketDetail, JiraPreset jiraPreset) {
        ResponseEntity<TicketDetail> response = null;
        try {
            HttpHeaders headers = this.getHttpHeaders(jiraPreset);

            String jsonRequest = new ObjectMapper().writer().withDefaultPrettyPrinter().writeValueAsString(ticketDetail);

            // build the request
            HttpEntity request = new HttpEntity(jsonRequest, headers);

            // make an HTTP GET request with headers
            response = restTemplate.exchange(postTicketUrl, HttpMethod.POST, request, TicketDetail.class);
            if (response.getStatusCode().equals(HttpStatus.CREATED)) {
                return response.getBody();
            }
        } catch (Exception e) {
            LOGGER.error("Exception occurred on Jira Creation ", e);
            TicketDetail ticketDetailError = new TicketDetail();
            ticketDetailError.setErrorMessage(e.getMessage());
            return ticketDetailError;
        }
        TicketDetail ticketDetailError = new TicketDetail();
        ticketDetailError.setErrorMessage(response.getStatusCode().toString());
        return ticketDetailError;
    }

    public TicketDetail addCommentToTicket(TicketDetail ticketDetail, JiraPreset jiraPreset) {
        ResponseEntity<TicketDetail> response;
        try {
            HttpHeaders headers = this.getHttpHeaders(jiraPreset);

            String jsonRequest = new ObjectMapper().writer().withDefaultPrettyPrinter().writeValueAsString(ticketDetail);

            HttpEntity request = new HttpEntity(jsonRequest, headers);

            response = restTemplate.exchange(addCommentUrl, HttpMethod.POST, request, TicketDetail.class, ticketDetail.getKey());
            response.getBody().setKey(ticketDetail.getKey());
            return response.getBody();

        } catch (Exception e) {
            LOGGER.error("Exception occurred on Jira Creation ", e);
            TicketDetail ticketDetailError = new TicketDetail();
            ticketDetailError.setErrorMessage(e.toString());
            return ticketDetailError;
        }
    }

    public String addAttachmentToTicket(String fileName, String issueId, JiraPreset jiraPreset) throws Exception {
        HttpHeaders headers = this.getHttpHeaders(jiraPreset);
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        headers.set("X-Atlassian-Token", "no-check");

        try {
            Map<String, String> params = new HashMap<>();
            params.put("issueId", issueId);

            ResponseEntity<String> response = restTemplate.exchange(addAttachmentUrl, HttpMethod.POST, new HttpEntity<>(this.getFileMultiValueMap(fileName), headers), String.class);
            return response.getBody();
        } catch (HttpClientErrorException e) {
            e.printStackTrace();
        }
        return null;
    }

    public String getTicket(String summary, JiraPreset jiraPreset,String jobName) {
        ResponseEntity<Object> response = null;
        try {
            HttpHeaders headers = this.getHttpHeaders(jiraPreset);
            String url = getTicketsBySummaryURL;
            url = url.replace("$summary", summary);
            String decodedUrl = URLDecoder.decode(url, "UTF-8");

            HttpEntity request = new HttpEntity("issues", headers);

            LOGGER.info("Getting Jira Tickets by Summary..");
            response = restTemplate.exchange(decodedUrl, HttpMethod.GET, request, Object.class);

            String ticketNo = null;
            for(LinkedHashMap obj:
                    (ArrayList<LinkedHashMap>) ((LinkedHashMap) response.getBody()).get("issues"))
            {
                String summaryName = ((LinkedHashMap) obj.get("fields")).get("summary").toString();
                String exactSummaryName = summaryName.substring(32);
                exactSummaryName = exactSummaryName.replaceAll(" Executed","");
                if(jobName.equals(exactSummaryName)) {
                    ticketNo = obj.get("key").toString();
                    break;
                }
            }
            return ticketNo;

//            return ((LinkedHashMap) ((ArrayList) ((LinkedHashMap) response.getBody()).get("issues")).get(0)).get("key").toString();

        } catch (Exception e) {
            LOGGER.error("Exception occurred on Getting Jira Tickets by Summary ", e);
            return null;
        }
    }
}
