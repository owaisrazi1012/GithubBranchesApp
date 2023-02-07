package com.nisum.jira.domain.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Fields {
    private String summary;
    private IssueType issuetype;
    private Project project;
    private Description description;
    private Reporter reporter;
    private String dueDate;
    private Assignee assignee;
    private Priority priority;
}
