# Test Automation Platform (TAP)

## Overview

* TAP is responsible for automate the testing process.
    - Fetch all branches from provided repos link.
    - Run test scenarios.
    - Generate reports.
    - Jira ticket create if any failure occurs in build formation.
    - Notification of failure send to defined Team and Slack channel.
    
   
## Running local

- `git clone https://gitlab.mynisum.com/navinash/GithubBranchesApp.git`
- `cd GithubBranchesApp`
- `mvn spring-boot:run`


***To run the application in local environment you need to define active profile in application.properties  `SPRING_PROFILES_ACTIVE= {dev or qa} `.***

## Deployment on Jenkins
- Login to Jenkins [Dashboard](http://20.39.52.170:8080/)
- For DEV env, Click GithubBranchesApp
- For QA env, Click GithubBranchesApp-qa
- In Configure window define branch name in 'Branch Specifier'
- Apply and save changes
- Click on 'Build Now' for deployment

## Elastic Search 
- QA  : https://qa-tap.mynisum.com:9200/
- DEV : http://dev-tap.mynisum.com:9200/

## Swagger
- QA  : [qa-swagger-ui-link](https://qa-tap.mynisum.com:8083/GithubBranchesApp/swagger-ui/index.html#)
- DEV : [dev-swagger-ui-link](https://dev-tap.mynisum.com:8083/GithubBranchesApp/swagger-ui/index.html#)
