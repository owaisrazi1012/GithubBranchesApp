package com.nisum;

import com.cdancy.jenkins.rest.JenkinsClient;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;


@SpringBootApplication
@EnableScheduling
public class RepositoryBranchesApplication {

//	@Value("${jenkins.url}")
//	private String jenkinsUrl;
//
//	@Value("${jenkins.credentials}")
//	private String jenkinsCredentials;

	@Bean
	public RestTemplate restTemplate() {
		return new RestTemplate();
	
	}
	@Bean
	public WebClient webClient() {
		return WebClient.builder()
				//.baseUrl("https://api.github.com")
//				.filter(ExchangeFilterFunctions
//						.basicAuthentication("username", "token"))
				.build();
	}

//	@Bean
//	JenkinsClient jenkinsClient() {
//		return JenkinsClient.builder()
//				.endPoint(jenkinsUrl) // Optional. Defaults to http://127.0.0.1:8080
//				.credentials(jenkinsCredentials) // need to provide username:apiToken or username:password
//				.build();
//	}
	@Bean
	public OpenAPI customOpenAPI() {
		return new OpenAPI()
				.info(new Info().title("GithubBranchesApp").version("1.0.0"))
				// Components section defines Security Scheme "mySecretHeader"
				.components(new Components()
						.addSecuritySchemes("token", new SecurityScheme()
								.type(SecurityScheme.Type.APIKEY)
								.in(SecurityScheme.In.HEADER)
								.name("Authorization")))
				// AddSecurityItem section applies created scheme globally
				.addSecurityItem(new SecurityRequirement().addList("token"));
	}

	public static void main(String[] args) {
		SpringApplication.run(RepositoryBranchesApplication.class, args);
	}

}
