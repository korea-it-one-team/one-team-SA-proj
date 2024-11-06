package com.lyj.proj.oneteamsaproj.crawl;

import com.lyj.proj.oneteamsaproj.vo.GameSchedule;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.*;

@Component
public class GameScheduleCrawl {
    public static Map<String, List<Map<String, Object>>> crawl() {
        Map<String, Map<String, List<GameSchedule>>> scheduleMap = new HashMap<>();

        System.setProperty("webdriver.chrome.driver", "C:/work_oneteam/one-team-SA-proj/chromedriver-win64/chromedriver.exe");

        ChromeOptions options = new ChromeOptions();
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");

        WebDriver driver = new ChromeDriver(options);

        try {
            // 현재 날짜 기준으로 -7일부터 +7일까지 크롤링
            for (int i = -7; i <= 7; i++) {
                // 날짜 계산
                Calendar calendar = Calendar.getInstance();
                calendar.add(Calendar.DATE, i);
                Date date = calendar.getTime();

                // URL 생성
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                String formattedDate = sdf.format(date);
                String url = "https://m.sports.naver.com/wfootball/schedule/index?date=" + formattedDate;

                driver.get(url);
                Thread.sleep(1000); // 1초 대기

                // 리그별 경기 리스트 저장
                List<WebElement> leagueItems = driver.findElements(By.className("ScheduleAllType_match_list_group__1nFDy"));

                for (WebElement leagueItem : leagueItems) {
                    // 리그 이름 추출
                    String leagueName = leagueItem.findElement(By.className("ScheduleAllType_title___Qfd4")).getText();
                    List<WebElement> matchItems = leagueItem.findElements(By.className("MatchBox_match_item__3_D0Q"));
                    List<GameSchedule> matchList = new ArrayList<>();

                    for (WebElement matchItem : matchItems) {
                        // 경기 시간
                        String startDate = matchItem.findElement(By.className("MatchBox_time__nIEfd")).getText();

                        // 홈팀과 원정팀 정보
                        List<WebElement> teamElements = matchItem.findElements(By.className("MatchBoxTeamArea_team_item__3w5mq"));
                        if (teamElements.size() >= 2) {
                            WebElement homeTeamElement = teamElements.get(0);
                            String homeTeam = homeTeamElement.findElement(By.className("MatchBoxTeamArea_team__3aB4O")).getText();
                            String homeTeamScore = getTeamScore(homeTeamElement);

                            WebElement awayTeamElement = teamElements.get(1);
                            String awayTeam = awayTeamElement.findElement(By.className("MatchBoxTeamArea_team__3aB4O")).getText();
                            String awayTeamScore = getTeamScore(awayTeamElement);

                            // 스코어가 N/A인 경우 빈 문자열로 처리
                            homeTeamScore = "N/A".equals(homeTeamScore) ? "" : homeTeamScore;
                            awayTeamScore = "N/A".equals(awayTeamScore) ? "" : awayTeamScore;

                            // GameSchedule 객체 생성 및 리스트에 추가
                            GameSchedule gameSchedule = new GameSchedule(startDate, homeTeam, awayTeam, homeTeamScore, awayTeamScore);
                            matchList.add(gameSchedule);
                        } else {
                            System.out.println("팀 정보가 부족합니다: " + startDate);
                        }
                    }

                    // 해당 리그와 날짜에 경기가 있을 경우에만 map에 추가
                    if (!matchList.isEmpty()) {
                        scheduleMap.putIfAbsent(formattedDate, new HashMap<>());
                        scheduleMap.get(formattedDate).put(leagueName, matchList);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace(); // 오류 발생 시 스택 트레이스 출력
        } finally {
            driver.quit(); // 브라우저 종료
        }

        // 날짜별로 리그 정보를 리스트로 변환
        Map<String, List<Map<String, Object>>> result = new HashMap<>();
        for (String date : scheduleMap.keySet()) {
            Map<String, List<GameSchedule>> leagues = scheduleMap.get(date);
            List<Map<String, Object>> leagueMatchesList = new ArrayList<>();

            for (String league : leagues.keySet()) {
                List<GameSchedule> matches = leagues.get(league);
                Map<String, Object> leagueInfo = new HashMap<>();
                leagueInfo.put("leagueName", league);
                leagueInfo.put("matches", matches);
                leagueMatchesList.add(leagueInfo);
            }

            result.put(date, leagueMatchesList);
        }
        return result;
    }

    private static String getTeamScore(WebElement teamElement) {
        try {
            return teamElement.findElement(By.className("MatchBoxTeamArea_score__1_YFB")).getText();
        } catch (Exception e) {
            return "N/A"; // 스코어가 없는 경우
        }
    }
}


