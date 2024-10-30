package com.lyj.proj.oneteamsaproj.service;

import com.lyj.proj.oneteamsaproj.repository.GameScheduleRepository;
import com.lyj.proj.oneteamsaproj.repository.WinDrawLoseRepository;
import com.lyj.proj.oneteamsaproj.vo.GameSchedule;
import com.lyj.proj.oneteamsaproj.vo.WinDrawLose;
import com.lyj.proj.oneteamsaproj.vo.ResultData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class WinDrawLoseService {

    @Autowired
    private WinDrawLoseRepository winDrawLoseRepository;

    @Autowired
    private MemberService memberService;

    @Autowired
    private GameScheduleService gameScheduleService;

    @Transactional
    public void savePrediction(WinDrawLose winDrawLose) {
        winDrawLoseRepository.insertPrediction(winDrawLose);
        updateMemberPoints(winDrawLose.getMemberId(), winDrawLose.getGameId());
    }

    public WinDrawLose getPrediction(int gameId, int memberId) {
        return winDrawLoseRepository.findByGameIdAndMemberId(gameId, memberId);
    }

    private void updateMemberPoints(int memberId, int gameId) {
        GameSchedule gameSchedule = gameScheduleService.getGameScheduleById(gameId);
        if (gameSchedule != null) {
            String actualResult = getActualResult(gameSchedule.getHomeTeamScore(), gameSchedule.getAwayTeamScore());
            WinDrawLose winDrawLose = winDrawLoseRepository.findByGameIdAndMemberId(gameId, memberId);

            if (winDrawLose != null && winDrawLose.getPrediction().equals(actualResult)) {
                memberService.addPoints(memberId, 5);
            }
        }
    }

    private String getActualResult(String homeTeamScore, String awayTeamScore) {
        // 스코어가 null이거나 빈 문자열인지 확인하여 빈 문자열 반환
        if (homeTeamScore == null || homeTeamScore.isEmpty() || awayTeamScore == null || awayTeamScore.isEmpty()) {
            return ""; // 빈 문자열 반환

        } else {
            // 스코어가 문자열 타입이라 정수로 형변환
            int homeTeamScoreInt = Integer.parseInt(homeTeamScore);
            int awayTeamScoreInt = Integer.parseInt(awayTeamScore);

            if (homeTeamScoreInt > awayTeamScoreInt) {
                return "승";
            } else if (homeTeamScoreInt < awayTeamScoreInt) {
                return "패";
            } else {
                return "무";
            }
        }
    }
}
