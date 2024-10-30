package com.lyj.proj.oneteamsaproj.repository;

import com.lyj.proj.oneteamsaproj.vo.GameSchedule;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface GameScheduleRepository {


    @Select("SELECT * FROM gameSchedule WHERE id = #{gameId}")
    GameSchedule findById(int gameId);

    // 모든 경기 일정을 조회하는 메서드
    @Select("SELECT * FROM gameSchedule ORDER BY startDate ASC")
    List<GameSchedule> findAll();

    // 특정 시작 날짜, 리그 시간, 리그 이름, 홈팀, 원정팀에 해당하는 경기를 조회하는 메서드
    // 필요한 이유는 같은 경기가 있다면 update를 해야하기때문에 insert 말고
    @Select("SELECT * FROM gameSchedule WHERE startDate = #{startDate} AND matchTime = #{matchTime} AND leagueName = #{leagueName} AND homeTeam = #{homeTeam} AND awayTeam = #{awayTeam}")
    GameSchedule findByStartDateAndMatchTimeAndLeagueNameAndHomeTeamAndAwayTeam(
            @Param("startDate") String startDate,  // 시작 날짜
            @Param("matchTime") String matchTime,  // 경기 시작 시간
            @Param("leagueName") String leagueName, // 리그명
            @Param("homeTeam") String homeTeam,     // 홈팀
            @Param("awayTeam") String awayTeam       // 원정팀
    );

    // 새로운 경기를 DB에 삽입하는 메서드
    @Insert("INSERT INTO gameSchedule (startDate, matchTime, leagueName, homeTeam, awayTeam, homeTeamScore, awayTeamScore) VALUES (#{startDate}, #{matchTime}, #{leagueName}, #{homeTeam}, #{awayTeam}, #{homeTeamScore}, #{awayTeamScore})")
    void insert(GameSchedule gameSchedule); // GameSchedule 객체를 파라미터로 받음

    // 기존 경기의 점수를 업데이트하는 메서드
    @Update("UPDATE gameSchedule SET homeTeamScore = #{homeTeamScore}, awayTeamScore = #{awayTeamScore} WHERE startDate = #{startDate} AND leagueName = #{leagueName} AND homeTeam = #{homeTeam} AND awayTeam = #{awayTeam}")
    void update(GameSchedule gameSchedule); // GameSchedule 객체를 파라미터로 받음
}


