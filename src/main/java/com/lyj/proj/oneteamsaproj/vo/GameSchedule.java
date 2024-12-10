package com.lyj.proj.oneteamsaproj.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GameSchedule {
    private int id;
    private String startDate;
    private String matchTime;
    private String leagueName;
    private String homeTeam;
    private String awayTeam;
    private String homeTeamScore;
    private String awayTeamScore;

    // id를 제외한 다른 필드만 받는 생성자 추가
    public GameSchedule(String startDate, String matchTime, String leagueName, String homeTeam, String awayTeam, String homeTeamScore, String awayTeamScore) {
        this.startDate = startDate;
        this.matchTime = matchTime;
        this.leagueName = leagueName;
        this.homeTeam = homeTeam;
        this.awayTeam = awayTeam;
        this.homeTeamScore = homeTeamScore;
        this.awayTeamScore = awayTeamScore;
    }
}
