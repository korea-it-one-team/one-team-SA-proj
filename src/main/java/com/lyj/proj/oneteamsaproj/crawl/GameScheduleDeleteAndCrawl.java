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

// 팀명이 바뀌어서 같은경기라고 인식 못할경우 -> 스케줄 테이블 날리고 크롤링 시작 날짜를 10-19일로 맞춰야한다.
// 그래야 경기의 id가 안 바뀌기 때문에 승무패 예측 정보 오류가 안생김.
// 해당 클래스는 스케줄 테이블 날리고 나서 크롤링 시작 날짜를 10-19일로 설정하고 insert 시킬때의 클래스.
@Component
public class GameScheduleDeleteAndCrawl {

    public static Map<String, List<Map<String, Object>>> crawl() {
        Map<String, Map<String, List<GameSchedule>>> scheduleMap = new HashMap<>();

        // 운영 체제에 따른 Chromedriver 경로 설정
        String os = System.getProperty("os.name").toLowerCase();
        if (os.contains("win")) {
            System.setProperty("webdriver.chrome.driver", "C:/work_oneteam/one-team-SA-proj/chromedriver-win64/chromedriver.exe");
        } else {
            // AWS 배포용(리눅스용 크롬드라이버 설치 후 경로 봐야해서 그때 수정해야함)
            System.setProperty("webdriver.chrome.driver", "/usr/local/bin/chromedriver");
        }

        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless"); // 필요 시 주석 해제
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");

        WebDriver driver = new ChromeDriver(options);

        try {
            // 고정된 시작 날짜를 10월 19일로 설정
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.MONTH, Calendar.OCTOBER);  // 10월
            calendar.set(Calendar.DAY_OF_MONTH, 19);         // 19일

            // 현재 날짜 기준으로 7일 후 날짜 계산
            Calendar endDate = Calendar.getInstance();
            endDate.add(Calendar.DATE, 7); // 현재 날짜 +7일

            // 10-19일부터 현재 날짜 +7일까지 크롤링
            while (!calendar.after(endDate)) {
                Date date = calendar.getTime();
                String formattedDate = sdf.format(date);

                // 크롤링 URL 설정
                String startDate = formattedDate;
                String url = "https://m.sports.naver.com/wfootball/schedule/index?date=" + formattedDate;

                driver.get(url);
                Thread.sleep(1000);

                // 리그별 경기 리스트 저장
                List<WebElement> leagueItems = driver.findElements(By.className("ScheduleAllType_match_list_group__1nFDy"));

                for (WebElement leagueItem : leagueItems) {
                    String leagueName = leagueItem.findElement(By.className("ScheduleAllType_title___Qfd4")).getText();
                    List<WebElement> matchItems = leagueItem.findElements(By.className("MatchBox_match_item__3_D0Q"));
                    List<GameSchedule> matchList = new ArrayList<>();

                    for (WebElement matchItem : matchItems) {
                        String fullMatchTime = matchItem.findElement(By.className("MatchBox_time__nIEfd")).getText();
                        String matchTime = fullMatchTime.split("\n")[1];

                        List<WebElement> teamElements = matchItem.findElements(By.className("MatchBoxTeamArea_team_item__3w5mq"));
                        if (teamElements.size() >= 2) {
                            String homeTeam = teamElements.get(0).findElement(By.className("MatchBoxTeamArea_team__3aB4O")).getText();
                            String homeTeamScore = getTeamScore(teamElements.get(0));
                            String awayTeam = teamElements.get(1).findElement(By.className("MatchBoxTeamArea_team__3aB4O")).getText();
                            String awayTeamScore = getTeamScore(teamElements.get(1));

                            homeTeamScore = "N/A".equals(homeTeamScore) ? "" : homeTeamScore;
                            awayTeamScore = "N/A".equals(awayTeamScore) ? "" : awayTeamScore;

                            GameSchedule gameSchedule = new GameSchedule(startDate, matchTime, leagueName, homeTeam, awayTeam, homeTeamScore, awayTeamScore);
                            matchList.add(gameSchedule);
                        }
                    }

                    if (!matchList.isEmpty()) {
                        scheduleMap.putIfAbsent(formattedDate, new HashMap<>());
                        scheduleMap.get(formattedDate).put(leagueName, matchList);
                    }
                }
                // 날짜를 다음 날로 증가
                calendar.add(Calendar.DATE, 1);
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            driver.quit();
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