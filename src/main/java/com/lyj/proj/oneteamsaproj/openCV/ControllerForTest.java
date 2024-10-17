package com.lyj.proj.oneteamsaproj.openCV;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Controller
public class ControllerForTest {

    FlaskClient flaskClient = new FlaskClient();

    @GetMapping("openCV/test")
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

        if (fileGenerated) {
            System.out.println("파일이 성공적으로 생성되었으므로 요청을 보냅니다. 파일 크기: " + fileSize);
            return "test";  // JSP 페이지 반환
        } else {
            model.addAttribute("error", "이미지 처리에 실패하였습니다.");
            return "error"; // 에러 페이지 경로
        }
    }
}

// 이미지 파일 경로
//            String imagePath = "src/main/resources/static/images/gray_image.jpg";
//            File imageFile = new File(imagePath);

//            // 파일이 생성될 때까지 기다리기
//            int attempts = 0;
//            while (!imageFile.exists() && attempts < 100) {  // 최대 100번 시도
//                System.out.println("파일이 생성되기를 기다리고 있습니다.");
//                Thread.sleep(500); // 0.5초 대기
//                attempts++;
//            }

//            // 파일이 존재하면 JSP 파일 반환
//            if (imageFile.exists()) {
//                System.out.println("파일이 생성되었습니다. 시도 횟수 : " + attempts);
//                return "test"; // JSP 파일 경로
//            } else {
//                // 실패한 경우 에러 페이지로 리다이렉트하거나, 적절한 메시지를 표시
//                model.addAttribute("error", "이미지 처리에 실패하였습니다.");
//                return "error"; // 에러 페이지 경로 (필요시)
//            }