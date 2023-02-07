package com.nisum.jira.domain.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TicketDetail implements Serializable {
    private String expand;
    private String id;
    private String self;
    private String key;
    private Fields fields;
    private Description body;


    private List<Object> errorMessages = new ArrayList<>();
    private Error error;
    private String errorMessage;
}

