package com.nisum.jira.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Assignee {
    private String id;
    private String self;
    private String accountId;
    private String displayName;
    private boolean active;
    private String timeZone;
    private String accountType;
}