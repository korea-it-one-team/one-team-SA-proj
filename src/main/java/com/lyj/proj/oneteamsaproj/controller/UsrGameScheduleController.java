package com.lyj.proj.oneteamsaproj.controller;

import com.lyj.proj.oneteamsaproj.crawl.GameScheduleCrawl;
import com.lyj.proj.oneteamsaproj.vo.GameSchedule;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
import java.util.Map;

@Controller
public class UsrGameScheduleController {

    private final GameScheduleCrawl gameScheduleCrawl;

    @Autowired
    public UsrGameScheduleController(GameScheduleCrawl gameScheduleCrawl) {
        this.gameScheduleCrawl = gameScheduleCrawl;
    }

    @GetMapping("usr/home/gameSchedule")
    public String getNews(Model model) {
        return "usr/home/gameSchedule"; // JSP 파일 경로
    }

    @GetMapping("/getGameSchedule")
    @ResponseBody
    public Map<String, List<Map<String, Object>>> getGameSchedule() {
        return gameScheduleCrawl.crawl(); // 크롤링
    }
}

