package com.nisum.auth.domain.dto;

import lombok.Data;

@Data
public class ClientLoginRequest {

    private String emailId;

    private String client;

}
