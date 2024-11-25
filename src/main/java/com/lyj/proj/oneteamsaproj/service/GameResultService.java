package com.lyj.proj.oneteamsaproj.service;

import com.lyj.proj.oneteamsaproj.repository.WinDrawLoseRepository;
import com.lyj.proj.oneteamsaproj.vo.WinDrawLose;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class GameResultService {

    private final WinDrawLoseRepository winDrawLoseRepository;
    private final MemberService memberService;

    @Autowired
    public GameResultService(WinDrawLoseRepository winDrawLoseRepository, MemberService memberService) {
        this.winDrawLoseRepository = winDrawLoseRepository;
        this.memberService = memberService;
    }

    @Transactional
    public void processGameResult(int gameId, String homeTeamScore, String awayTeamScore) {
        String actualResult = getActualResult(homeTeamScore, awayTeamScore);

        List<WinDrawLose> predictions = winDrawLoseRepository.findByGameId(gameId);

        for (WinDrawLose prediction : predictions) {
            if (prediction.getPrediction().equals(actualResult)) {
                memberService.addPoints(prediction.getMemberId(), 5); // 포인트 부여
            }
        }
    }

    private String getActualResult(String homeTeamScore, String awayTeamScore) {
        if (homeTeamScore == null || homeTeamScore.isEmpty() || awayTeamScore == null || awayTeamScore.isEmpty()) {
            return "";
        }

        // DB에 저장된 스코어는 문자열 타입이기 때문에 정수화 시켜야한다.
        int homeScore = Integer.parseInt(homeTeamScore);
        int awayScore = Integer.parseInt(awayTeamScore);

        if (homeScore > awayScore) return "승";
        if (homeScore < awayScore) return "패";
        return "무";
    }
}

