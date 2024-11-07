package com.lyj.proj.oneteamsaproj.openCV;

import org.springframework.web.client.RestTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;

public class TrainingService {

    private final RestTemplate restTemplate = new RestTemplate();

    public void startYoloTraining() {
        String url = "http://127.0.0.1:5000/start-training";
        HttpHeaders headers = new HttpHeaders();
        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);
        System.out.println("YOLO training response: " + response.getBody());
    }
}
