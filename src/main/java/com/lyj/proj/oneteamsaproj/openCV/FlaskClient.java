package com.lyj.proj.oneteamsaproj.openCV;

import org.springframework.core.io.FileSystemResource;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.SocketException;

public class FlaskClient {

    private Process flaskProcess;

    // Flask 서버 시작
    public void startFlaskServer() {
        try {
            ProcessBuilder pb = new ProcessBuilder("C:\\work_oneteam\\one-team-SA-proj\\venv\\Scripts\\python.exe",
                    "C:\\work_oneteam\\one-team-SA-proj\\src\\openCV\\test.py");
            pb.redirectErrorStream(true); // 에러와 표준 출력을 합침
            flaskProcess = pb.start();

            // 서버 출력 로그를 확인하여 서버가 완전히 기동되었는지 확인
            new Thread(() -> {
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(flaskProcess.getInputStream()))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        System.out.println(line); // 서버 로그 출력
                        if (line.contains("Running on")) {
                            System.out.println("Flask 서버가 정상적으로 시작되었습니다.");
                            break; // 서버가 정상적으로 시작되었으므로 대기 종료
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }).start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Flask 서버 종료
    public void stopFlaskServer() {
        try {
            // Flask 서버에 종료 요청 보내기 (GET 요청)
            RestTemplate restTemplate = new RestTemplate();
            String shutdownUrl = "http://127.0.0.1:5000/shutdown";
            restTemplate.getForEntity(shutdownUrl, String.class);
            System.out.println("Flask 서버 종료 요청을 보냈습니다.");
        } catch (ResourceAccessException e) {
            // 연결이 리셋되었을 경우 서버가 종료된 것으로 간주하고 처리
            if (e.getCause() instanceof SocketException) {
                System.out.println("Flask 서버가 정상적으로 종료되었습니다.");
            } else {
                e.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean sendRequest() {
        // Flask 서버 시작
        startFlaskServer();

        // 서버 준비 상태 확인 및 요청 전송
        boolean serverStarted = false;
        RestTemplate restTemplate = new RestTemplate();
        int attempts = 0;

        // 최대 10번 시도, 1초 간격으로 서버 확인
        while (!serverStarted && attempts < 10) {
            try {
                String flaskUrl = "http://127.0.0.1:5000/health";  // health 체크를 위한 경로
                System.out.println("Flask URL: " + flaskUrl);
                ResponseEntity<String> response = restTemplate.getForEntity(flaskUrl, String.class);

                if (response.getStatusCode().is2xxSuccessful()) {
                    serverStarted = true;
                    System.out.println("Flask 서버가 정상적으로 응답 중입니다.");
                }
            } catch (HttpClientErrorException e) {
                if (e.getStatusCode() == HttpStatus.METHOD_NOT_ALLOWED) {
                    // 405 오류 발생 시 서버는 실행 중인 것으로 간주
                    serverStarted = true;
                    System.out.println("Flask 서버가 실행 중이며, POST 요청을 받아들일 준비가 되었습니다.");
                } else {
                    attempts++;
                    System.out.println("Flask 서버가 아직 준비되지 않았습니다. 재시도 중...");
                    e.printStackTrace();
                }
            } catch (Exception e) {
                attempts++;
                System.out.println("Flask 서버가 아직 준비되지 않았습니다. 재시도 중...");
                e.printStackTrace();
            }

            try {
                Thread.sleep(1000); // 1초 대기
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }
        }

        if (!serverStarted) {
            System.out.println("Flask 서버 시작에 실패하였습니다.");
            return false;
        } else {
            // Flask 서버가 실행 중일 때 POST 요청을 보냄
            String imagePath = "C:\\work_oneteam\\one-team-SA-proj\\src\\main\\resources\\static\\images\\test-icon.jpg";
            FileSystemResource imageFile = new FileSystemResource(imagePath);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);

            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("image", imageFile);

            HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);
            String flaskUrl = "http://localhost:5000/process"; // 이건 POST 요청을 보낼 URL
            try {
                ResponseEntity<byte[]> response = restTemplate.postForEntity(flaskUrl, requestEntity, byte[].class);

                byte[] imageBytes = response.getBody();
                if (imageBytes != null) {
                    try (FileOutputStream fos = new FileOutputStream("C:\\work_oneteam\\one-team-SA-proj\\src\\main\\resources\\static\\images\\gray_image.jpg")) {
                        fos.write(imageBytes);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            // 작업이 끝나면 Flask 서버 종료
            stopFlaskServer();
            return true;
        }
    }
}

