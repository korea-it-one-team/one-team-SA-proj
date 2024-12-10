package com.lyj.proj.oneteamsaproj.openCV;

import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;

@RestController
@RequestMapping("/api/training")  // '/api/training' 경로를 컨트롤러에 매핑
public class TrainingController {

    private final String flaskServerUrl = "http://localhost:5000/StudyTest";  // Flask 서버 URL

    // POST 요청을 처리하는 메소드
    @PostMapping("/start")  // '/api/training/start' 경로를 처리
    public ResponseEntity<String> startTraining(@RequestBody TrainingRequest request) {
        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<TrainingRequest> entity = new HttpEntity<>(request, headers);

        // Flask 서버에 POST 요청을 보냄
        ResponseEntity<String> response = restTemplate.exchange(
                flaskServerUrl, HttpMethod.POST, entity, String.class);

        return ResponseEntity.ok("Training started on Flask server");
    }
}
