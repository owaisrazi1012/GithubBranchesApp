package com.nisum.service.impl;

import com.nisum.service.SlackService;
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
public class SlackServiceImpl implements SlackService {
    private static final Logger LOGGER = LoggerFactory.getLogger(SlackServiceImpl.class);

    @Value("${slack.url}")
    private String slackURL;
    @Autowired
    RestTemplate restTemplate;
    @Override
    public void sendMessageToSlack(String message) {
        Map<String,String> stringBuilder = new HashMap<>();
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);
        stringBuilder.put("text",message);
        HttpEntity<Map<String,String>> request= new HttpEntity<>(stringBuilder,httpHeaders);
        LOGGER.info("Sending Notification to Slack...");
        restTemplate.postForEntity(slackURL,request,String.class);
    }
}
