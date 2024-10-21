package com.lyj.proj.oneteamsaproj.openCV;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ControllerForTest {

    FlaskClient flaskClient = new FlaskClient();

    @GetMapping("usr/openCV/test")
    public String openCVtest(Model model) {
        flaskClient.sendRequest();
        return "/usr/openCV/test"; // JSP 파일 경로
    }
}
