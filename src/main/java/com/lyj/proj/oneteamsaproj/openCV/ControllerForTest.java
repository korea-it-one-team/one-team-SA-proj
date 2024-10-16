package com.lyj.proj.oneteamsaproj.openCV;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import java.io.File;

@Controller
public class ControllerForTest {

    FlaskClient flaskClient = new FlaskClient();

    @GetMapping("openCV/test")
    public String openCVtest(Model model) throws InterruptedException {

        // 요청 전송 및 완료 여부 확인
        boolean isProcessingComplete = flaskClient.sendRequest();

        // 이미지 처리 완료 후 JSP 페이지 반환
        if (isProcessingComplete) {
            // 이미지 파일 경로
            String imagePath = "C:\\work_oneteam\\one-team-SA-proj\\src\\main\\resources\\static\\images\\gray_image.jpg";
            File imageFile = new File(imagePath);

            // 파일이 생성될 때까지 기다리기
            int attempts = 0;
            while (!imageFile.exists() && attempts < 10) {  // 최대 10번 시도
                Thread.sleep(500); // 0.5초 대기
                attempts++;
            }

            // 파일이 존재하면 JSP 파일 반환
            if (imageFile.exists()) {
                return "test"; // JSP 파일 경로
            } else {
                // 실패한 경우 에러 페이지로 리다이렉트하거나, 적절한 메시지를 표시
                model.addAttribute("error", "이미지 처리에 실패하였습니다.");
                return "error"; // 에러 페이지 경로 (필요시)
            }
        } else {
            model.addAttribute("error", "이미지 처리에 실패하였습니다.");
            return "error"; // 에러 페이지 경로 (필요시)
        }
    }
}
