package com.lyj.proj.oneteamsaproj.controller;

import com.lyj.proj.oneteamsaproj.crawl.NewsCrawl;
import com.lyj.proj.oneteamsaproj.vo.News;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@Controller
public class UsrNewsController {

    private final NewsCrawl newsCrawl;

    @Autowired
    public UsrNewsController(NewsCrawl newsCrawl) {
        this.newsCrawl = newsCrawl;
    }

    // 뉴스 페이지 요청 처리
    @GetMapping("usr/home/news")
    public String getNews(Model model) {
        return "usr/home/news"; // JSP 파일 경로
    }

    @GetMapping("/getNews")
    @ResponseBody
    public List<News> getNews() {
        return newsCrawl.crawl(); // 뉴스 크롤링
    }
}
