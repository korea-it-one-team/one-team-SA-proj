package com.lyj.proj.oneteamsaproj.controller;

import com.lyj.proj.oneteamsaproj.crawl.GameScheduleCrawl;
import com.lyj.proj.oneteamsaproj.service.GameScheduleService;
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

    private final GameScheduleService gameScheduleService;

    @Autowired
    public UsrGameScheduleController(GameScheduleService gameScheduleService) {
        this.gameScheduleService = gameScheduleService;
    }
    // JSP 접근 메서드
    @GetMapping("usr/home/gameSchedule")
    public String getGameSchedulePage(Model model) {
        List<GameSchedule> gameSchedules = gameScheduleService.getAllGameSchedules();
        model.addAttribute("gameSchedules", gameSchedules);
        return "usr/home/gameSchedule"; // JSP 파일 경로
    }

    // DB에 저장할 메서드
    @GetMapping("/getGameSchedule")
    @ResponseBody
    public Map<String, List<Map<String, Object>>> getGameSchedule() {
        return gameScheduleService.crawlAndSaveGameSchedules();
    }
}


