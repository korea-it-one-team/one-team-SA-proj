package com.lyj.proj.oneteamsaproj.controller;

import com.lyj.proj.oneteamsaproj.service.GameScheduleService;
import com.lyj.proj.oneteamsaproj.service.WinDrawLoseService;
import com.lyj.proj.oneteamsaproj.vo.GameSchedule;
import com.lyj.proj.oneteamsaproj.vo.ResultData;
import com.lyj.proj.oneteamsaproj.vo.WinDrawLose;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
import java.util.Map;

@Controller
public class UsrGameScheduleController {

    private final GameScheduleService gameScheduleService;
    private final WinDrawLoseService predictionService; // 예측 서비스 추가

    @Autowired
    public UsrGameScheduleController(GameScheduleService gameScheduleService, WinDrawLoseService predictionService) {
        this.gameScheduleService = gameScheduleService;
        this.predictionService = predictionService;
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

    // 경기 예측 메서드
    @PostMapping("/predict")
    @ResponseBody
    public String predictGame(@RequestParam int gameId, @RequestParam int memberId, @RequestParam String prediction) {
        WinDrawLose winDrawLose = new WinDrawLose(0, gameId, memberId, prediction);
        gameScheduleService.savePrediction(winDrawLose);

        // 예측 성공 시 포인트 업데이트 로직 추가
        // 예시: gameScheduleService.updateMemberPoints(memberId, 5);

        return "success"; // AJAX 요청에 대한 응답
    }
}


