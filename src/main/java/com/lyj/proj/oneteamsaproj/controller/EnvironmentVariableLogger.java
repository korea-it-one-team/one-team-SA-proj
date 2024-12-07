package com.lyj.proj.oneteamsaproj.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Component
public class EnvironmentVariableLogger {

    @Value("${spring.cloud.gcp.storage.project-id}")
    private String projectId;

    @Value("${spring.cloud.gcp.storage.credentials.location}")
    private String classpath;

    @Value("${spring.cloud.gcp.storage.bucket}")
    private String bucketKey;

    @Value("${spring.mail.password}")
    private String emailKey;

    @Value("${google.application.credentials}")
    private String googleCredentials;

    @PostConstruct
    public void logEnvironmentVariables() {
        System.out.println("PROJECT_ID: " + projectId);
        System.out.println("CLASSPATH: " + classpath);
        System.out.println("BUCKET_KEY: " + bucketKey);
        System.out.println("EMAIL_KEY: " + emailKey);
        System.out.println("GOOGLE_APPLICATION_CREDENTIALS: " + googleCredentials);
    }
}
