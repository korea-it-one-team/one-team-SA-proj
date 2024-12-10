package com.lyj.proj.oneteamsaproj.exception;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(ArticleProcessingException.class)
    @ResponseBody
    public Map<String, String> handleArticleProcessingException(ArticleProcessingException ex) {
        Map<String, String> response = new HashMap<>();
        response.put("resultCode", "F-1");
        response.put("message", ex.getJsScript());
        return response;
    }
}