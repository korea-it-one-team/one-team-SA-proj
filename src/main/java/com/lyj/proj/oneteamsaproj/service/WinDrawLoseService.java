package com.lyj.proj.oneteamsaproj.service;

import com.lyj.proj.oneteamsaproj.repository.WinDrawLoseRepository;
import com.lyj.proj.oneteamsaproj.vo.GameSchedule;
import com.lyj.proj.oneteamsaproj.vo.WinDrawLose;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


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
        // 중복 예측 방지: 동일한 게임 ID와 회원 ID로 이미 예측이 있는지 확인
        WinDrawLose existingPrediction = winDrawLoseRepository.findByGameIdAndMemberId(
                winDrawLose.getGameId(), winDrawLose.getMemberId());

        if (existingPrediction != null) {
            throw new IllegalStateException("이미 해당 경기에 예측이 저장되어 있습니다.");
        }
        // 예측 저장
        winDrawLoseRepository.insertPrediction(winDrawLose);
        // 예측이 성공적으로 저장된 경우에만 포인트 업데이트
        updateMemberPoints(winDrawLose.getMemberId(), winDrawLose.getGameId());
    }

    public WinDrawLose getPrediction(int gameId, int memberId) {
        return winDrawLoseRepository.findByGameIdAndMemberId(gameId, memberId);
    }

    private void updateMemberPoints(int memberId, int gameId) {
        GameSchedule gameSchedule = gameScheduleService.getGameScheduleById(gameId);

        // 경기 일정이 존재하는지 확인
        if (gameSchedule != null) {
            String actualResult = getActualResult(gameSchedule.getHomeTeamScore(), gameSchedule.getAwayTeamScore());
            WinDrawLose winDrawLose = winDrawLoseRepository.findByGameIdAndMemberId(gameId, memberId);

            // 예측 정보와 실제 결과 비교하여 포인트 추가
            if (winDrawLose != null && winDrawLose.getPrediction().equals(actualResult)) {
                memberService.addPoints(memberId, 5);
            }
        }
    }

    private String getActualResult(String homeTeamScore, String awayTeamScore) {
        // 스코어가 null이거나 빈 문자열인지 확인
        if (homeTeamScore == null || homeTeamScore.isEmpty() || awayTeamScore == null || awayTeamScore.isEmpty()) {
            return ""; // 경기 결과가 없는 경우 빈 문자열 반환
        }

        // 스코어가 문자열 타입이라 정수로 형변환
        int homeTeamScoreInt = Integer.parseInt(homeTeamScore);
        int awayTeamScoreInt = Integer.parseInt(awayTeamScore);

        // 경기 결과 계산
        if (homeTeamScoreInt > awayTeamScoreInt) {
            return "승";
        } else if (homeTeamScoreInt < awayTeamScoreInt) {
            return "패";
        } else {
            return "무";
        }
    }
}

