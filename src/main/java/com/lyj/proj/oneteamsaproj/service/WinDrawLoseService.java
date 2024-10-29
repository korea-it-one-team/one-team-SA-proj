package com.lyj.proj.oneteamsaproj.service;

import com.lyj.proj.oneteamsaproj.repository.GameScheduleRepository;
import com.lyj.proj.oneteamsaproj.repository.WinDrawLoseRepository;
import com.lyj.proj.oneteamsaproj.vo.GameSchedule;
import com.lyj.proj.oneteamsaproj.vo.WinDrawLose;
import com.lyj.proj.oneteamsaproj.vo.ResultData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class WinDrawLoseService {

    private final WinDrawLoseRepository predictionRepository;
    private final MemberService memberService; // 회원 서비스 추가
    private final GameScheduleRepository gameScheduleRepository;

    @Autowired
    public WinDrawLoseService(WinDrawLoseRepository predictionRepository, MemberService memberService, GameScheduleRepository gameScheduleRepository) {
        this.predictionRepository = predictionRepository;
        this.memberService = memberService;
        this.gameScheduleRepository = gameScheduleRepository;
    }

    public ResultData savePrediction(int memberId, int gameId, String prediction) {
        // 예측 저장
        predictionRepository.save(new WinDrawLose(memberId, gameId, prediction));

        // 경기 결과와 비교하여 포인트 추가
        GameSchedule gameSchedule = predictionRepository.findGameById(gameId);
        if (gameSchedule != null) {
            String actualResult = determineMatchResult(gameSchedule.getHomeTeamScore(), gameSchedule.getAwayTeamScore());
            if (actualResult.equals(prediction)) {
                memberService.addPoints(memberId, 5);
            }
        }
        return ResultData.from("S-1", "예측이 저장되었습니다.");
    }

    private String determineMatchResult(String homeScore, String awayScore) {
        int home = Integer.parseInt(homeScore);
        int away = Integer.parseInt(awayScore);
        if (home > away) return "승"; // 홈팀 승리
        else if (home < away) return "패"; // 원정팀 승리
        else return "무"; // 무승부
    }

    public List<GameSchedule> getAllGameSchedulesWithPredictions(int memberId) {
        List<GameSchedule> gameSchedules = gameScheduleRepository.findAll(); // 모든 경기 일정 조회
        for (GameSchedule gameSchedule : gameSchedules) {
            WinDrawLose prediction = predictionRepository.findPredictionByGameIdAndMemberId(gameSchedule.getId(), memberId);
            if (prediction != null) {
                gameSchedule.setUserPrediction(prediction.getPrediction()); // 예측 결과 설정
            }
        }
        return gameSchedules;
    }
}
