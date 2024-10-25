package com.lyj.proj.oneteamsaproj.openCV;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
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

//line for re-commit
@Controller
public class ControllerForTest {

    FlaskClient flaskClient = new FlaskClient();

    // 동영상 처리 요청을 받으면 처리 중 페이지로 리다이렉트
    @GetMapping("openCV/video-test")
    public String openCVVideoTest(Model model) {
        flaskClient.sendRequest("video");
        return "redirect:/processing";
    }

    // 처리 중 JSP
    @GetMapping("processing")
    public String processingPage() {
        return "processing";
    }

    @GetMapping("video-status")
    @ResponseBody
    public Map<String, String> videoStatus() {
        Map<String, String> status = new HashMap<>();
        Map<String, Object> flaskResponse = checkProcessingStatus(); // Flask에서 받은 전체 응답을 Object로 처리

        // 상태와 진행률을 처리
        status.put("status", (String) flaskResponse.get("status"));  // 상태 추가
        status.put("progress", String.valueOf(flaskResponse.get("progress")));  // 진행률을 String으로 변환 후 추가

        System.out.println("보내주는 status와 progress:" + status);
        return status;
    }

    // Flask 서버에서 상태를 받아오는 로직
    private Map<String, Object> checkProcessingStatus() {
        RestTemplate restTemplate = new RestTemplate();
        String url = "http://localhost:5000/video-status";  // Flask 서버의 상태 확인 API
        Map<String, Object> response = restTemplate.getForObject(url, Map.class);

        // 받은 데이터 로그로 출력
        System.out.println("Flask에서 받은 상태: " + response);

        return response;  // 전체 응답 반환
    }

    // 동영상 처리 완료 후 결과 페이지로 리다이렉트
    @GetMapping("openCV/result")
    public String resultPage(Model model) {

        String flaskUrl = "http://localhost:5000/download_video";  // Flask에서 결과 동영상을 다운로드할 수 있는 엔드포인트
        String videoPath = "src/main/resources/static/video/processed_video.mp4";  // 저장 경로

        // 동영상을 다운로드하여 저장하는 로직
        try {
            RestTemplate restTemplate = new RestTemplate();
            ResponseEntity<byte[]> response = restTemplate.getForEntity(flaskUrl, byte[].class);  // Flask로부터 동영상 요청
            byte[] videoBytes = response.getBody();
            if (videoBytes != null) {
                try (FileOutputStream fos = new FileOutputStream(videoPath)) {
                    fos.write(videoBytes);
                    fos.flush();
                    System.out.println("동영상이 성공적으로 저장되었습니다: " + videoPath);
                }
            }
        } catch (Exception e) {
            System.out.println("동영상 다운로드 중 예외 발생: " + e.getMessage());
            e.printStackTrace();
            flaskClient.stopFlaskServer();
            return "error";
        }

        // 동영상 파일이 저장되었는지 확인
        File videoFileAfterSave = new File(videoPath);
        if (videoFileAfterSave.exists()) {
            System.out.println("동영상이 성공적으로 생성되었습니다.");
            // 동영상 경로를 모델에 추가
            model.addAttribute("videoSrc", "/video/processed_video.mp4");
            // Flask 서버 종료
            flaskClient.stopFlaskServer();
            return "result";  // 결과 페이지로 리다이렉트
        } else {
            System.out.println("동영상 파일 생성 실패.");
            // 파일이 없으면 에러 페이지로 리다이렉트하거나 에러 메시지를 표시
            model.addAttribute("errorMessage", "동영상 처리가 실패했습니다.");
            // Flask 서버 종료
            flaskClient.stopFlaskServer();
            return "error";  // 오류 페이지로 리다이렉트
        }
    }

    @GetMapping("openCV/img-test")
    public String openCVtest(Model model) throws InterruptedException {

        // 요청 전송 및 완료 여부 확인
        boolean isProcessingComplete = flaskClient.sendRequest("image");
        System.out.println("isProcessingComplete = " + isProcessingComplete);

        Path imagePath = Paths.get("src/main/resources/static/images/gray_image.jpg");

        // 일정 시간 동안 파일의 크기가 0보다 커지는지 반복 확인
        int maxRetries = 10;
        int retries = 0;
        long fileSize = 0;
        boolean fileGenerated = false;

        // 파일 크기 확인 시 예외 처리 추가
        if(isProcessingComplete) {
            while (retries < maxRetries) {
                try {
                    fileSize = Files.size(imagePath);
                    if (fileSize > 0) {
                        System.out.println("파일이 성공적으로 생성되었습니다. 파일 크기: " + fileSize);
                        fileGenerated = true;
                        break;
                    }
                } catch (IOException e) {
                    System.out.println("파일 크기 확인 중 오류 발생: " + e.getMessage());
                }

                System.out.println("파일 크기 확인 중... 시도 " + retries + "회");
                Thread.sleep(500); // 500ms 대기
                retries++;
            }
        }

        if (fileGenerated) {
            System.out.println("파일이 성공적으로 생성되었으므로 요청을 보냅니다. 파일 크기: " + fileSize);
            model.addAttribute("imageSrc", "/images/gray_image.jpg");
            return "result";  // JSP 페이지 반환
        } else {
            model.addAttribute("error", "이미지 처리에 실패하였습니다.");
            return "error"; // 에러 페이지 경로
        }
    }
}
