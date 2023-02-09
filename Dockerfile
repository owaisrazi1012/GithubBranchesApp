FROM openjdk:8-jdk-alpine
MAINTAINER baeldung.com
COPY target/GithubBranchesApp-0.0.1-SNAPSHOT.jar GithubBranchesApp-0.0.1-SNAPSHOT.jar
ENTRYPOINT ["java","-jar","GithubBranchesApp-0.0.1-SNAPSHOT.jar"]
