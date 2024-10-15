package com.lyj.proj.oneteamsaproj.openCV;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.core.io.FileSystemResource;

import java.io.FileOutputStream;
import java.io.IOException;

public class FlaskClient {

    private Process flaskProcess;

    // Flask 서버 시작
    public void startFlaskServer() {
        try {
            ProcessBuilder pb = new ProcessBuilder("C:\\work_oneteam\\one-team-SA-proj\\venv\\Scripts\\python.exe", "C:\\work_oneteam\\one-team-SA-proj\\src\\openCV\\test.py");
            flaskProcess = pb.start();
            System.out.println("Flask 서버를 시작합니다...");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Flask 서버 종료
    public void stopFlaskServer() {
        if (flaskProcess != null && flaskProcess.isAlive()) {
            flaskProcess.destroy();
            System.out.println("Flask 서버가 종료되었습니다.");
        }
    }

    // Flask 서버에 요청을 보내는 메소드
    public boolean sendRequest() {

        // Flask 서버 시작
        startFlaskServer();

        // Flask 서버 상태 확인
        RestTemplate restTemplate = new RestTemplate();
        boolean serverStarted = false;
        int attempts = 0;

        // 최대 5번 시도, 1초 간격으로 서버 확인
        while (!serverStarted && attempts < 10) {
            try {
                restTemplate.getForEntity("http://localhost:5000", String.class);
                serverStarted = true;
            } catch (Exception e) {
                attempts++;
                try {
                    System.out.println("Flask 서버가 아직 완전히 시작되지 않았습니다.");
                    Thread.sleep(1000); // 1초 대기
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }
            }
        }

        if (!serverStarted) {
            System.out.println("Flask 서버 시작에 실패하였습니다.");
            return false;
        } else {
            // Flask 서버가 실행 중이므로 요청 보냄
            String imagePath = "C:\\work_oneteam\\one-team-SA-proj\\src\\main\\resources\\static\\images\\test-icon.jpg";
            FileSystemResource imageFile = new FileSystemResource(imagePath);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);

            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("image", imageFile);

            HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);
            String flaskUrl = "http://localhost:5000/process";
            ResponseEntity<byte[]> response = restTemplate.postForEntity(flaskUrl, requestEntity, byte[].class);

            try {
                byte[] imageBytes = response.getBody();
                FileOutputStream fos = new FileOutputStream("C:\\work_oneteam\\one-team-SA-proj\\src\\main\\resources\\static\\images\\gray_image.jpg");
                fos.write(imageBytes);
                fos.close();
            } catch (Exception e) {
                e.printStackTrace();
            }

            // Flask 서버 종료
            stopFlaskServer();
            return true;
        }
    }
}
