package com.lyj.proj.oneteamsaproj.openCV;

import org.springframework.core.io.FileSystemResource;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.io.*;
import java.net.SocketException;
import java.nio.file.*;
import java.util.Scanner;

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

    public boolean sendRequest(String code) {
        // Flask 서버 시작
        startFlaskServer();

        // 서버 준비 상태 확인 및 요청 전송
        boolean serverStarted = false;
        RestTemplate restTemplate = new RestTemplate();
        int attempts = 0;

        // 최대 10번 시도, 1초 간격으로 서버 확인
        while (!serverStarted && attempts < 20) {
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
                }
            } catch (Exception e) {
                attempts++;
                System.out.println("Flask 서버가 아직 준비되지 않았습니다. 재시도 중...");
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
            if(code.equals("image")) {
                return processImage(restTemplate);
            } else if(code.equals("video")) {
                return processVideo(restTemplate);
            } else {
                System.out.println("처리 요청 불가능한 형식입니다.");
                return false;
            }
        }
    }

    public boolean processVideo(RestTemplate restTemplate) {
        String flaskUrl = "http://localhost:5000/process_video";
        String videoPath = "src/main/resources/static/video/temp_video.mp4";  // 업로드할 동영상 경로
        FileSystemResource videoFile = new FileSystemResource(videoPath);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        String homeTeam = "";
        String awayTeam = "";

        Scanner scanner = new Scanner(System.in);

        while(homeTeam.isEmpty() || homeTeam.contains("Retry")) {
            System.out.flush();
            System.out.print("Enter the home team: ");
            homeTeam = scanner.nextLine().trim();
        }

        while(awayTeam.isEmpty() || awayTeam.contains("Retry")) {
            System.out.flush();
            System.out.print("Enter the away team: ");
            awayTeam = scanner.nextLine().trim();
        }

        System.out.println("home team: " + homeTeam);
        System.out.println("away team: " + awayTeam);

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("video", videoFile);
        body.add("home_team", homeTeam);
        body.add("away_team", awayTeam);

        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

        try {
            // 동영상 처리가 시작되었음을 로그로 기록
            System.out.println("동영상 처리를 시작합니다. 파일 경로: " + videoPath);

            // 동영상 처리 요청을 비동기 스레드로 전송
            new Thread(() -> {
                try {
                    // Flask로 POST 요청 보내기
                    ResponseEntity<String> response = restTemplate.postForEntity(flaskUrl, requestEntity, String.class);
                    System.out.println("Flask로 동영상 처리를 시작하였습니다. 응답: " + response.getBody());
                } catch (Exception e) {
                    System.out.println("동영상 처리 중 예외 발생: " + e.getMessage());
                    e.printStackTrace();
                }
            }).start();
            scanner.close();
            return true;
        } catch (Exception e) {
            System.out.println("동영상 처리 요청 중 예외 발생: " + e.getMessage());
            e.printStackTrace();
            scanner.close();
            return false;
        }
    }

    public boolean processImage(RestTemplate restTemplate) {
        // Flask 서버가 실행 중일 때 POST 요청을 보냄
        String imagePath = "src/main/resources/static/images/test-icon.jpg";
        FileSystemResource imageFile = new FileSystemResource(imagePath);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("image", imageFile);

        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);
        String flaskUrl = "http://localhost:5000/process"; // 이건 POST 요청을 보낼 URL

        try {
            System.out.println("POST 요청을 보냅니다. 파일 경로: " + imagePath);
            ResponseEntity<byte[]> response = restTemplate.postForEntity(flaskUrl, requestEntity, byte[].class);
            byte[] imageBytes = response.getBody();
            if (imageBytes != null) {
                System.out.println("이미지 응답을 수신했습니다. 이미지를 저장합니다.");
                try (FileOutputStream fos = new FileOutputStream("src/main/resources/static/images/gray_image.jpg")) {
                    fos.write(imageBytes);
                    fos.flush();
                    System.out.println("이미지가 gray_image.jpg로 성공적으로 저장되었습니다.");
                }
            }
        } catch (Exception e) {
            System.out.println("이미지 처리 중 예외 발생: " + e.getMessage());
            e.printStackTrace();
        }

        // 이미지가 저장된 후, 파일 존재 여부 확인
        File imageFileAfterSave = new File("src/main/resources/static/images/gray_image.jpg");
        if (imageFileAfterSave.exists()) {
            System.out.println("파일이 성공적으로 생성되었습니다.");
            // 작업이 끝나면 Flask 서버 종료
            stopFlaskServer();
            return true;
        } else {
            System.out.println("파일 생성 실패.");
            // 작업이 끝나면 Flask 서버 종료
            stopFlaskServer();
            return false; // 파일 존재하지 않으면 실패 처리
        }
    }
}