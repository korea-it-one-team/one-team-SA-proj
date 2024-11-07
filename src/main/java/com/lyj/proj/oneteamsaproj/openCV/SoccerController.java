package com.lyj.proj.oneteamsaproj.openCV;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.ModelAndView;

import java.util.Map;

@RestController
@RequestMapping("/api/soccer")
public class SoccerController {

    private final RestTemplate restTemplate;
    private final String flaskServerUrl = "http://localhost:5000"; // Flask 서버 URL

    @Autowired
    public SoccerController(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @PostMapping("/downloadTracking")
    public ResponseEntity<Map<String, Object>> downloadTracking(@RequestBody Map<String, String> task) {
        String url = flaskServerUrl + "/download_tracking";
        ResponseEntity<Map<String, Object>> response = restTemplate.postForEntity(url, task, (Class<Map<String, Object>>) (Class<?>) Map.class);
        return response;
    }

    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getServerStatus() {
        String url = flaskServerUrl + "/status";
        ResponseEntity<Map<String, Object>> response = restTemplate.getForEntity(url, (Class<Map<String, Object>>) (Class<?>) Map.class);
        return response;
    }

    private final String flaskStreamUrl = "http://localhost:5000/video_feed";

    @GetMapping("/streamTracking")
    public ModelAndView streamTracking() {
        ModelAndView mav = new ModelAndView();
        mav.setViewName("/stream"); // 경로를 /WEB-INF/jsp/usr/stream.jsp에 맞춤
        mav.addObject("streamUrl", "http://localhost:5000/video_feed"); // Flask 스트림 URL 전달
        return mav;
    }

}

