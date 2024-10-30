package com.lyj.proj.oneteamsaproj.service;

import com.lyj.proj.oneteamsaproj.crawl.GameScheduleCrawl;
import com.lyj.proj.oneteamsaproj.repository.GameScheduleRepository;
import com.lyj.proj.oneteamsaproj.vo.GameSchedule;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class GameScheduleService {

    private final GameScheduleRepository gameScheduleRepository; // 경기를 저장하고 조회하는 리포지토리
    private final GameScheduleCrawl gameScheduleCrawl; // 경기 일정을 크롤링하는 서비스

    @Autowired
    public GameScheduleService(GameScheduleRepository gameScheduleRepository, GameScheduleCrawl gameScheduleCrawl) {
        this.gameScheduleRepository = gameScheduleRepository;
        this.gameScheduleCrawl = gameScheduleCrawl;
    }

    public GameSchedule getGameScheduleById(int gameId) {
        return gameScheduleRepository.findById(gameId);
    }

    // 모든 경기 일정을 조회하는 메서드
    public List<GameSchedule> getAllGameSchedules() {
        return gameScheduleRepository.findAll(); // 레포지토리에서 모든 경기 일정 반환
    }

    // 크롤링하여 경기 일정을 저장하는 메서드
    public Map<String, List<Map<String, Object>>> crawlAndSaveGameSchedules() {
        Map<String, List<Map<String, Object>>> scheduleData = gameScheduleCrawl.crawl(); // 경기 일정 크롤링

        // 크롤링한 데이터의 날짜를 기준으로 반복
        for (String date : scheduleData.keySet()) {
            // 각 리그 데이터를 반복
            for (Map<String, Object> leagueData : scheduleData.get(date)) {
                List<?> matches = (List<?>) leagueData.get("matches"); // 리그의 경기 목록을 가져옴

                // 각 경기를 반복
                for (Object match : matches) {
                    // match가 GameSchedule 객체인지 확인
                    if (match instanceof GameSchedule) {
                        GameSchedule matchSchedule = (GameSchedule) match; // GameSchedule로 캐스팅

                        // DB에서 기존 경기 검색
                        GameSchedule existingGame = gameScheduleRepository.findByStartDateAndMatchTimeAndLeagueNameAndHomeTeamAndAwayTeam(
                                matchSchedule.getStartDate(), matchSchedule.getMatchTime(), matchSchedule.getLeagueName(), matchSchedule.getHomeTeam(), matchSchedule.getAwayTeam()
                        );

                        try {
                            // DB에 기존 경기가 없으면 새로운 경기를 DB에 삽입
                            if (existingGame == null) {
                                gameScheduleRepository.insert(matchSchedule);
                            } else {
                                // DB에 기존 경기가 있으면 점수를 업데이트
                                existingGame.setHomeTeamScore(matchSchedule.getHomeTeamScore());
                                existingGame.setAwayTeamScore(matchSchedule.getAwayTeamScore());
                                gameScheduleRepository.update(existingGame); // 업데이트
                            }
                        } catch (Exception e) {
                            // 삽입 또는 업데이트 중 오류가 발생한 경우
                            System.err.println("Error inserting/updating matchSchedule: " + e.getMessage());
                        }
                    } else {
                        // match가 유효한 GameSchedule이 아닌 경우
                        System.err.println("match is not a valid GameSchedule: " + match);
                    }
                }
            }
        }
        System.out.println("scheduleData : " + scheduleData); // 크롤링한 데이터 출력
        return scheduleData; // 크롤링한 데이터 반환
    }
}

