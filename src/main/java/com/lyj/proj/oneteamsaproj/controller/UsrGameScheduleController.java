package com.lyj.proj.oneteamsaproj.controller;

import com.lyj.proj.oneteamsaproj.service.GameScheduleService;
import com.lyj.proj.oneteamsaproj.service.WinDrawLoseService;
import com.lyj.proj.oneteamsaproj.vo.GameSchedule;
import com.lyj.proj.oneteamsaproj.vo.Member;
import com.lyj.proj.oneteamsaproj.vo.Rq;
import com.lyj.proj.oneteamsaproj.vo.WinDrawLose;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class UsrGameScheduleController {

    private final GameScheduleService gameScheduleService;
    private WinDrawLoseService winDrawLoseService; // 예측 서비스 추가
    private final Rq rq;

    @Autowired
    public UsrGameScheduleController(GameScheduleService gameScheduleService, WinDrawLoseService winDrawLoseService, Rq rq) {
        this.gameScheduleService = gameScheduleService;
        this.winDrawLoseService = winDrawLoseService;
        this.rq = rq;

    }

    // JSP 접근 메서드
    @GetMapping("usr/home/gameSchedule")
    public String getGameSchedulePage(Model model) {
        List<GameSchedule> gameSchedules = gameScheduleService.getAllGameSchedules();

        // 현재 날짜와 시간 가져오기
        LocalDateTime now = LocalDateTime.now();

        // 포맷 설정 (예: "yyyy-MM-dd'T'HH:mm")
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");
        String formattedDateTime = now.format(formatter);

        // 현재 로그인한 회원의 id를 가져와서 경기예측정보를 모델에 추가
        int memberId = rq.getLoginedMemberId();
        List<WinDrawLose> userPredictions = winDrawLoseService.getPrediction(memberId);

        // 각 경기 일정에 맞는 예측 정보를 Map으로 변환하여 모델에 추가
        Map<Integer, String> predictionsMap = new HashMap<>();
        for (WinDrawLose prediction : userPredictions) {
            predictionsMap.put(prediction.getGameId(), prediction.getPrediction());
        }

        model.addAttribute("userPredictionsMap", predictionsMap);
        model.addAttribute("currentDateTime", formattedDateTime);
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

        Map<String, String> response = new HashMap<>();

        try {
            winDrawLoseService.savePrediction(winDrawLose);
            response.put("redirectUrl", "/usr/home/gameSchedule"); // 리다이렉트할 URL 설정
            return ResponseEntity.ok(response);
        } catch (IllegalStateException ex) {
            // 예외 발생 시 클라이언트에 적절한 오류 메세지 전달
            // -> WinDrawLoseService 클래스의 이미 존재한다면 DB에 추가 못하게에 해당하는 메세지
            response.put("error", ex.getMessage());
            return ResponseEntity.badRequest().body(response);
        } catch (Exception ex) {
            // 기타 예외 처리
            response.put("error", "예기치 않은 오류가 발생했습니다.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}


