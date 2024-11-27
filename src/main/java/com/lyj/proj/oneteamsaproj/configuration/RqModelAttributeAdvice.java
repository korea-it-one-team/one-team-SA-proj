package com.lyj.proj.oneteamsaproj.configuration;

import com.lyj.proj.oneteamsaproj.vo.Rq;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

// ControllerAdvice에서 Thymeleaf에 전달
@ControllerAdvice
public class RqModelAttributeAdvice {
    @ModelAttribute("rq")
    public Rq addRqToModel(HttpServletRequest request) {
        Rq rq = (Rq) request.getAttribute("rq");
        System.out.println("Rq Thymeleaf 전달 상태: " + (rq != null));
        return (Rq) request.getAttribute("rq");
    }
}
