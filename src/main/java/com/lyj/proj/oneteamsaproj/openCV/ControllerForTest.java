package com.lyj.proj.oneteamsaproj.openCV;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ControllerForTest {

    FlaskClient flaskClient = new FlaskClient();

    @GetMapping("openCV/test")
    public String openCVtest(Model model) {

        // 요청 전송
        flaskClient.sendRequest();

        return "test"; // JSP 파일 경로
    }
}

