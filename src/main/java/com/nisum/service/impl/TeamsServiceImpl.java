package com.nisum.service.impl;

import com.nisum.service.TeamsService;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
@Profile({"dev", "qa"})
public class TeamsServiceImpl implements TeamsService {
    private static final Logger LOGGER = LoggerFactory.getLogger(TeamsServiceImpl.class);

    @Value("${teams.url}")
    private String teamsURL;
    @Autowired
    RestTemplate restTemplate;
    @Override
    public void sendMessageToTeams(String message) {
        Map<String,String> stringBuilder = new HashMap<>();
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);
        stringBuilder.put("text",message);
        HttpEntity<Map<String,String>> request= new HttpEntity<>(stringBuilder,httpHeaders);
        LOGGER.info("Sending Notification to teams...");
        restTemplate.postForEntity(teamsURL,request,String.class);
    }
}
