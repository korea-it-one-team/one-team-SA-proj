package com.lyj.proj.oneteamsaproj.openCV;

import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpHeaders;

import java.io.FileOutputStream;

public class FlaskClient {

    public void sendRequest() {
        RestTemplate restTemplate = new RestTemplate();

        // 요청에 이미지 파일 추가 (Flask로 전송할 이미지 파일 경로 설정)
        String imagePath = "C:\\work_oneteam\\one-team-SA-proj\\src\\main\\resources\\static\\images\\test-icon.jpg";
        FileSystemResource imageFile = new FileSystemResource(imagePath);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("image", imageFile);

        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

        // Flask 서버의 이미지 처리 API 호출(서버 포트 바꿔야 함)
        String flaskUrl = "http://localhost:5000/process";
        ResponseEntity<byte[]> response = restTemplate.postForEntity(flaskUrl, requestEntity, byte[].class);

        // Flask 서버에서 받은 흑백 이미지를 저장할 경로 설정
        try {
            byte[] imageBytes = response.getBody();
            FileOutputStream fos = new FileOutputStream("C:\\work_oneteam\\one-team-SA-proj\\src\\main\\resources\\static\\images\\gray_image.jpg");
            fos.write(imageBytes);
            fos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
