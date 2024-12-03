package com.lyj.proj.oneteamsaproj.openCV;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

//line for re-commit
@Controller
public class ControllerForTest {

    private final RestTemplate restTemplate;
    private static final String FLASK_DOWNLOAD_URL = "http://localhost:5000/video/download_video";
    private static final String VIDEO_SAVE_PATH = "src/main/resources/static/video/processed_video_h264.mp4";
    private static final String FLASK_IMAGE_PROCESS_URL = "http://localhost:5000/image/process-image";
    private static final String IMAGE_SAVE_PATH = "src/main/resources/static/images/gray_image.jpg";

    @Autowired
    public ControllerForTest(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    // 동영상 처리 요청을 보내고 처리 중 페이지로 리다이렉트
    @GetMapping("openCV/video-test")
    public String openCVVideoTest() {
        System.out.println("openCV video test");

        String flaskUrl = "http://localhost:5000/video/process_video";

        System.out.println("flaskUrl: " + flaskUrl);

        // 요청 생성
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("video", new FileSystemResource("src/main/resources/static/video/temp_video.mp4"));
        body.add("home_team", "Arsenal");
        body.add("away_team", "Liverpool");

        System.out.println("body" + body);

        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(flaskUrl, requestEntity, String.class);
            System.out.println("Flask 응답: " + response.getBody());
        } catch (Exception e) {
            System.err.println("Flask 요청 중 오류 발생: " + e.getMessage());
            e.printStackTrace();
            return "usr/openCV/error";
        }

        return "redirect:/processing";
    }

    // 처리 중 thymeleaf
    @GetMapping("processing")
    public String processingPage() {
        return "usr/openCV/processing";
    }

    @GetMapping("video-status")
    @ResponseBody
    public Map<String, String> videoStatus() {
        Map<String, String> status = new HashMap<>();

        try {
            Map<String, Object> flaskResponse = checkProcessingStatus();

            // 상태와 진행률 처리
            status.put("status", (String) flaskResponse.get("status"));
            status.put("progress", String.valueOf(flaskResponse.get("progress")));

            // 로그 기록
            System.out.println("보내주는 status와 progress:" + status);
        } catch (Exception e) {
            // 예외 발생 시 에러 상태 반환
            status.put("status", "error");
            status.put("progress", "0");
            System.err.println("Flask 상태 확인 중 오류 발생: " + e.getMessage());
        }
        return status;
    }

    private Map<String, Object> checkProcessingStatus() {
        String url = "http://localhost:5000/video/video-status"; // Flask 서버 상태 확인 API

        try {
            Map<String, Object> response = restTemplate.getForObject(url, Map.class);
            System.out.println("Flask에서 받은 상태: " + response);
            return response; // 정상적으로 반환
        } catch (Exception e) {
            System.err.println("Flask 상태 요청 실패: " + e.getMessage());
            throw e; // 상위 호출 메서드에서 처리
        }
    }

    @GetMapping("openCV/result")
    public String resultPage(Model model) {

        try {
            Map<String, Object> flaskStatus = restTemplate.getForObject("http://localhost:5000/video/video-status", Map.class);
            if (!"completed".equals(flaskStatus.get("status"))) {
                if("error".equals(flaskStatus.get("status"))) {
                    model.addAttribute("errorMessage", "flask에서 동영상 처리에 실패하였습니다.");
                    return "usr/openCV/error";
                }
                model.addAttribute("errorMessage", "동영상 처리가 완료되지 않았습니다.");
                return "usr/openCV/processing";
            }

            // Flask 서버에서 동영상 다운로드
            if (downloadProcessedVideo()) {
                model.addAttribute("videoSrc", "/video/processed_video_h264.mp4");
                System.out.println("동영상 다운로드 및 저장 성공.");
                return "usr/openCV/result";  // 결과 페이지로 이동
            } else {
                model.addAttribute("errorMessage", "동영상 처리가 실패했습니다.");
                System.out.println("동영상 다운로드 실패.");
                return "usr/openCV/error";  // 에러 페이지로 이동
            }
        } catch (Exception e) {
            model.addAttribute("errorMessage", "예외 발생: " + e.getMessage());
            System.err.println("동영상 다운로드 중 예외 발생: " + e.getMessage());
            e.printStackTrace();
            return "usr/openCV/error";  // 에러 페이지로 이동
        }
    }

    private boolean downloadProcessedVideo() {
        try {
            // Flask 서버로 GET 요청 보내기
            ResponseEntity<byte[]> response = restTemplate.getForEntity(FLASK_DOWNLOAD_URL, byte[].class);

            // 응답 상태 확인
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                // 파일 저장
                saveVideoToFile(response.getBody(), VIDEO_SAVE_PATH);
                return true;
            } else {
                System.err.println("Flask 서버 응답 실패. 상태 코드: " + response.getStatusCode());
                return false;
            }
        } catch (IOException e) {
            System.err.println("Flask 서버로부터 동영상 다운로드 중 IOException 발생: " + e.getMessage());
            return false; // IOException 발생 시 실패로 처리
        } catch (Exception e) {
            System.err.println("Flask 서버 요청 중 일반 예외 발생: " + e.getMessage());
            throw new RuntimeException("Flask 서버 요청 중 예외 발생", e); // 런타임 예외로 감싸서 처리
        }
    }

    private void saveVideoToFile(byte[] videoBytes, String path) throws IOException {
        try (FileOutputStream fos = new FileOutputStream(path)) {
            fos.write(videoBytes);
            fos.flush();
            System.out.println("동영상이 성공적으로 저장되었습니다: " + path);
        }
    }

    @GetMapping("openCV/img-test")
    public String openCVtest(Model model) {
        try {
            // Flask 서버에 이미지 처리 요청 전송
            if (sendImageProcessRequest()) {
                System.out.println("이미지 처리 요청 성공. Flask에서 결과를 확인 중...");

                // Flask에서 처리 완료 후 이미지 다운로드
                if (waitForImageProcessing()) {
                    model.addAttribute("imageSrc", "/images/gray_image.jpg");
                    System.out.println("이미지 다운로드 및 저장 성공.");
                    return "usr/openCV/result";  // JSP 페이지 반환
                } else {
                    model.addAttribute("error", "이미지 처리에 실패했습니다.");
                    System.err.println("이미지 처리 실패.");
                    return "usr/openCV/error";  // 에러 페이지 경로
                }
            } else {
                model.addAttribute("error", "Flask 서버에 이미지 요청을 보낼 수 없습니다.");
                System.err.println("Flask 이미지 요청 실패.");
                return "usr/openCV/error";
            }
        } catch (Exception e) {
            model.addAttribute("error", "예외 발생: " + e.getMessage());
            System.err.println("이미지 처리 중 예외 발생: " + e.getMessage());
            e.printStackTrace();
            return "usr/openCV/error";  // 에러 페이지 경로
        }
    }

    private boolean sendImageProcessRequest() {
        FileSystemResource imageFile = new FileSystemResource("src/main/resources/static/images/test-icon.jpg");
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("image", imageFile);

        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(FLASK_IMAGE_PROCESS_URL, requestEntity, String.class);
            return response.getStatusCode() == HttpStatus.OK;
        } catch (Exception e) {
            System.err.println("Flask 서버에 이미지 처리 요청 중 오류 발생: " + e.getMessage());
            return false;
        }
    }

    private boolean waitForImageProcessing() throws InterruptedException, IOException {
        Path imagePath = Paths.get(IMAGE_SAVE_PATH);
        int maxRetries = 10;
        int retries = 0;

        // 주기적으로 파일 생성 여부 확인
        while (retries < maxRetries) {
            if (Files.exists(imagePath) && Files.size(imagePath) > 0) {
                System.out.println("파일이 성공적으로 생성되었습니다. 파일 크기: " + Files.size(imagePath));
                return true;
            }
            System.out.println("파일 생성 대기 중... 시도 " + retries);
            Thread.sleep(500);  // 500ms 대기
            retries++;
        }
        return false;  // 파일이 생성되지 않음
    }
}
