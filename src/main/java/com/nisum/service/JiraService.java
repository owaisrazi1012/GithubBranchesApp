package com.nisum.service;

import com.nisum.api.preset.domain.entity.JiraPreset;
import com.nisum.jira.domain.dto.TicketDetail;

public interface JiraService {
    TicketDetail createJiraTicket(TicketDetail ticketDetail, JiraPreset jiraPreset);

    TicketDetail addCommentToTicket(TicketDetail ticketDetail, JiraPreset jiraPreset);

    String addAttachmentToTicket(String fileName, String issueId, JiraPreset jiraPreset) throws Exception;

    String getTicket(String summary, JiraPreset jiraPreset,String jobName);
}

