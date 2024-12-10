package com.lyj.proj.oneteamsaproj.controller;

import com.lyj.proj.oneteamsaproj.service.GameScheduleService;
import com.lyj.proj.oneteamsaproj.service.LoginService;
import com.lyj.proj.oneteamsaproj.service.WinDrawLoseService;
import com.lyj.proj.oneteamsaproj.vo.GameSchedule;
import com.lyj.proj.oneteamsaproj.vo.Member;
import com.lyj.proj.oneteamsaproj.utils.RqUtil;
import com.lyj.proj.oneteamsaproj.vo.WinDrawLose;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class UsrGameScheduleController {

    private final GameScheduleService gameScheduleService;
    private WinDrawLoseService winDrawLoseService; // 예측 서비스 추가
    private final RqUtil rq;

    @Autowired
    private LoginService loginService;

    @Autowired
    public UsrGameScheduleController(GameScheduleService gameScheduleService, WinDrawLoseService winDrawLoseService, RqUtil rq) {
        this.gameScheduleService = gameScheduleService;
        this.winDrawLoseService = winDrawLoseService;
        this.rq = rq;
    }

    // JSP 접근 메서드
    @GetMapping("usr/home/gameSchedule")
    public String getGameSchedulePage(Model model) {

        List<GameSchedule> gameSchedules = gameScheduleService.getAllGameSchedules();

        //비어있으면 DB에 다시 넣고 다시 가져오기
        if(gameSchedules.isEmpty()) {
            insertGameSchedule();
            gameSchedules = gameScheduleService.getAllGameSchedules();
        }

        // 현재 날짜와 시간 가져오기
        LocalDateTime now = LocalDateTime.now();
        // 포맷 설정 (예: "yyyy-MM-dd'T'HH:mm")
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");
        String formattedDateTime = now.format(formatter);
        // 현재 로그인한 회원의 id를 가져와서 경기예측정보를 모델에 추가
        int memberId = loginService.getLoginedMemberId();
        List<WinDrawLose> userPredictions = winDrawLoseService.getPrediction(memberId);
        // 각 경기 일정에 맞는 예측 정보를 Map으로 변환하여 모델에 추가
        Map<Integer, String> predictionsMap = new HashMap<>();
        for (WinDrawLose prediction : userPredictions) {
            predictionsMap.put(prediction.getGameId(), prediction.getPrediction());
        }

        model.addAttribute("userPredictionsMap", predictionsMap);
        model.addAttribute("currentDateTime", formattedDateTime);
        model.addAttribute("gameSchedules", gameSchedules);

        return "usr/home/gameSchedule"; // 타임리프 파일 경로
    }

    // 승/무/패 예측 AJAX POST 요청 메서드
    @PostMapping("/predict")
    public ResponseEntity<Map<String, Object>> savePrediction(@RequestParam int gameId,
                                                              @RequestParam int memberId,
                                                              @RequestParam String prediction) {
        WinDrawLose winDrawLose = new WinDrawLose();
        winDrawLose.setGameId(gameId);
        winDrawLose.setMemberId(memberId);
        winDrawLose.setPrediction(prediction);

        Map<String, Object> response = new HashMap<>(); // 응답 객체를 Map<String, Object>로 변경

        try {
            // 예측 저장
            winDrawLoseService.savePrediction(winDrawLose);

            // 예측한 경기 정보 가져오기
            GameSchedule gameSchedule = gameScheduleService.findById(gameId);

            response.put("redirectUrl", "/usr/home/gameSchedule"); // 리다이렉트할 URL 설정
            response.put("gameSchedule", gameSchedule); // 예측한 경기 정보를 응답에 추가

            return ResponseEntity.ok(response);
        } catch (IllegalStateException ex) {
            // 예외 발생 시 클라이언트에 적절한 오류 메세지 전달
            response.put("error", ex.getMessage());
            return ResponseEntity.badRequest().body(response);
        } catch (Exception ex) {
            // 기타 예외 처리
            response.put("error", "예기치 않은 오류가 발생했습니다.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }


    @GetMapping("/usr/home/gameSchedule/date")
    @ResponseBody
    public Map<String, Object> getGameSchedulesByDate(@RequestParam("date") String date, HttpSession session) {
        LocalDate selectedDate = LocalDate.parse(date);
        List<GameSchedule> schedules = gameScheduleService.getGameSchedulesByDate(selectedDate);
        // 로그인된 회원 정보 가져오기
        Member loginedMember = loginService.getLoginedMember();

        // 예측 정보를 가져오기 (로그인된 경우)
        Map<Integer, String> userPredictionsMap = new HashMap<>();
        if (loginedMember != null) {
            List<WinDrawLose> userPredictions = winDrawLoseService.getPrediction(loginedMember.getId());
            for (WinDrawLose prediction : userPredictions) {
                userPredictionsMap.put(prediction.getGameId(), prediction.getPrediction());
            }
        }

        Map<String, Object> response = new HashMap<>();
        response.put("schedules", schedules);
        response.put("loginedMember", loginedMember);
        response.put("userPredictionsMap", userPredictionsMap);

        return response;
    }

    // DB에 저장할 메서드
    @GetMapping("/getGameSchedule")
    @ResponseBody
    public Map<String, List<Map<String, Object>>> getGameSchedule() {
        return gameScheduleService.crawlAndSaveGameSchedules();
    }

    // 팀명이 달라서 경기정보가 오류가 났을 때 스케줄 DB 날리고 경기일정 id 맞춰서 DB에 저장할 메서드
    @GetMapping("/insertGameSchedule")
    @ResponseBody
    public Map<String, List<Map<String, Object>>> insertGameSchedule() {
        return gameScheduleService.deleteAndCrawlGameSchedules();
    }
}