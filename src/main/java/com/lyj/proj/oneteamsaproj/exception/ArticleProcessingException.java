package com.lyj.proj.oneteamsaproj.exception;

public class ArticleProcessingException extends RuntimeException {
    private final String jsScript;

    public ArticleProcessingException(String jsScript) {
        super("Article processing exception occurred.");
        this.jsScript = jsScript;
    }

    public String getJsScript() {
        return jsScript;
    }
}
