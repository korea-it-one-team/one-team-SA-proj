package com.lyj.proj.oneteamsaproj.controller;

import com.lyj.proj.oneteamsaproj.service.GameScheduleService;
import com.lyj.proj.oneteamsaproj.service.WinDrawLoseService;
import com.lyj.proj.oneteamsaproj.vo.GameSchedule;
import com.lyj.proj.oneteamsaproj.vo.WinDrawLose;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class UsrGameScheduleController {

    private final GameScheduleService gameScheduleService;
    private WinDrawLoseService winDrawLoseService; // 예측 서비스 추가

    @Autowired
    public UsrGameScheduleController(GameScheduleService gameScheduleService, WinDrawLoseService winDrawLoseService) {
        this.gameScheduleService = gameScheduleService;
        this.winDrawLoseService = winDrawLoseService;
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

    // 승/무/패 예측 AJAX POST 요청 메서드
    @PostMapping("/predict")
    public ResponseEntity<Map<String, String>> savePrediction(@RequestParam int gameId,
                                                              @RequestParam int memberId,
                                                              @RequestParam String prediction) {
        WinDrawLose winDrawLose = new WinDrawLose();
        winDrawLose.setGameId(gameId);
        winDrawLose.setMemberId(memberId);
        winDrawLose.setPrediction(prediction);

        winDrawLoseService.savePrediction(winDrawLose);

        // 리다이렉트할 URL을 포함하여 응답
        Map<String, String> response = new HashMap<>();
        response.put("redirectUrl", "/usr/home/gameSchedule"); // 리다이렉트할 URL 설정

        return ResponseEntity.ok(response);
    }
}


