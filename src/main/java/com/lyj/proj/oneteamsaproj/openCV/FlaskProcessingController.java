package com.lyj.proj.oneteamsaproj.openCV;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lyj.proj.oneteamsaproj.exception.ArticleProcessingException;
import com.lyj.proj.oneteamsaproj.service.ArticleService;
import com.lyj.proj.oneteamsaproj.utils.Ut;
import com.lyj.proj.oneteamsaproj.vo.Article;
import com.lyj.proj.oneteamsaproj.vo.ResultData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

//line for re-commit
@Controller
public class FlaskProcessingController {

    private final RestTemplate restTemplate;
//    private static final String FLASK_IMAGE_PROCESS_URL = "http://localhost:5000/image/process-image";
//    private static final String IMAGE_SAVE_PATH = "src/main/resources/static/images/gray_image.jpg";
    private final ArticleService articleService;

    @Autowired
    public FlaskProcessingController(RestTemplate restTemplate, ArticleService articleService) {
        this.restTemplate = restTemplate;
        this.articleService = articleService;
    }

    // 동영상 처리 요청을 보내고 처리 중 페이지로 리다이렉트
    public ResultData videoProcess(int articleId, String videoFilePath, String homeTeam, String awayTeam) {
        System.out.println("openCV video test");

        String flaskUrl = "http://localhost:5000/video/process_video";

        System.out.println("flaskUrl: " + flaskUrl);

        // 요청 생성
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("video", new FileSystemResource(videoFilePath));
        body.add("home_team", homeTeam);
        body.add("away_team", awayTeam);
        body.add("article_id", articleId);  // 추가된 부분

        System.out.println("body: " + body);

        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

        try {
            // Flask 서버에 POST 요청
            ResponseEntity<String> response = restTemplate.postForEntity(flaskUrl, requestEntity, String.class);
            System.out.println("Flask 응답: " + response.getBody());

            // Flask 응답 본문 JSON 파싱
            String responseBody = response.getBody();
            ObjectMapper objectMapper = new ObjectMapper();
            Map<String, String> responseMap = objectMapper.readValue(responseBody, Map.class);

            // message 필드 확인
            String message = responseMap.get("message");
            System.out.println("Flask 응답 메시지: " + message);

            if (message != null && message.contains("동영상 처리")) {
                return new ResultData("S-1", "동영상 처리가 시작되었습니다.");
            } else {
                return new ResultData("F-Flask", "동영상 처리 시작에 실패하였습니다.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return new ResultData("F-Flask", "Flask 응답 처리 중 오류가 발생했습니다.");
        }
    }

    // 처리 중 thymeleaf
    @GetMapping("processing")
    public String showProcessingPage() {
        return "usr/openCV/processing";
    }

    @GetMapping("video-status")
    @ResponseBody
    public Map<String, String> videoStatus(@RequestParam int articleId) {

        Article article = articleService.getArticleById(articleId);
        if (article.getBoardId() != 4) {
            throw new ArticleProcessingException("동영상 처리 요청을 하지 않은 article입니다.");
        }

        Map<String, String> status = new HashMap<>();

        try {
            Map<String, Object> flaskResponse = checkProcessingStatus(articleId);

            // 상태와 진행률 처리
            status.put("status", (String) flaskResponse.get("status"));
            status.put("progress", String.valueOf(flaskResponse.get("progress")));

            System.out.println("보내주는 status와 progress: " + status);
        } catch (Exception e) {
            status.put("status", "error");
            status.put("progress", "0");
            System.err.println("Flask 상태 확인 중 오류 발생: " + e.getMessage());
        }
        return status;
    }

    private Map<String, Object> checkProcessingStatus(int articleId) {
        String url = "http://localhost:5000/video-status?article_id=" + articleId;

        try {
            Map<String, Object> response = restTemplate.getForObject(url, Map.class);
            System.out.println("Flask에서 받은 상태: " + response);
            return response;
        } catch (Exception e) {
            System.err.println("Flask 상태 요청 실패: " + e.getMessage());
            throw e;
        }
    }

    @PostMapping("/openCV/analysisCompleted")
    @ResponseBody
    public ResponseEntity<?> receiveProcessingStatus(@RequestBody Map<String, String> payload) {

        System.out.println("receiveProcessingStatus 실행됨");

        String articleId = payload.get("article_id");
        String status = payload.get("status");

        if (articleId == null || status == null) {
            return ResponseEntity.badRequest().body("Invalid payload. Missing article_id or status.");
        }

        // 처리 상태 로그
        System.out.println("Received status update from Flask:");
        System.out.println("Article ID: " + articleId);
        System.out.println("Status: " + status);

        try {
            // 해당 상태에 따라 추가 처리 수행
            if ("completed".equals(status)) {
                // 동영상과 요약 이미지 다운로드
                boolean filesDownloaded = downloadProcessedFiles(articleId);
                if (filesDownloaded) {
                    return ResponseEntity.ok("Processing completed and files downloaded.");
                } else {
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .body("Failed to download the processed files.");
                }
            } else if ("error".equals(status)) {
                // 에러 상태 처리
                System.err.println("Error during video processing for article " + articleId);
            }

            return ResponseEntity.ok("Status updated successfully.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Internal server error.");
        }
    }

    private boolean downloadProcessedFiles(String articleId) {
        boolean videoDownloaded = downloadFileFromFlask(articleId, "video");
        boolean summaryDownloaded = downloadFileFromFlask(articleId, "summary");

        return videoDownloaded && summaryDownloaded;
    }

    private boolean downloadFileFromFlask(String articleId, String fileType) {
        String flaskUrl = "http://localhost:5000/video/download_video?article_id=" + articleId + "&file_type=" + fileType;
        String savePath = "src/main/resources/static/" + fileType + "/processed_" + fileType + "_" + articleId + "." + (fileType.equals("video") ? "mp4" : "png");

        try {
            ResponseEntity<byte[]> response = restTemplate.getForEntity(flaskUrl, byte[].class);
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                saveFileToDisk(response.getBody(), savePath);
                return true;
            }
            System.err.println("Failed to download " + fileType + " from Flask.");
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private void saveFileToDisk(byte[] fileBytes, String path) throws IOException {
        try (FileOutputStream fos = new FileOutputStream(path)) {
            fos.write(fileBytes);
            fos.flush();
            System.out.println("File saved successfully: " + path);
        }
    }
}
