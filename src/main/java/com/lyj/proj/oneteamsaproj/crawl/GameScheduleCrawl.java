package com.lyj.proj.oneteamsaproj.crawl;

import com.lyj.proj.oneteamsaproj.vo.GameSchedule;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

@Component
public class GameScheduleCrawl {
    public static List<GameSchedule> crawl() {
        List<GameSchedule> matchList = new ArrayList<>();

        System.setProperty("webdriver.chrome.driver", "C:/work_oneteam/one-team-SA-proj/chromedriver-win64/chromedriver.exe");

        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless"); // 필요 시 주석 해제
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");

        WebDriver driver = new ChromeDriver(options);

        try {
            driver.get("https://m.sports.naver.com/wfootball/schedule/index?date=2024-10-19");

            List<WebElement> matchItems = driver.findElements(By.className("MatchBox_match_item__3_D0Q"));
            for (WebElement matchItem : matchItems) {
                // 경기 시간
                String startDate = matchItem.findElement(By.className("MatchBox_time__nIEfd")).getText();
                // 경기장
                String stadium = matchItem.findElement(By.className("MatchBox_stadium__13gft")).getText();

                // 홈팀 정보
                WebElement homeTeamElement = matchItem.findElements(By.className("MatchBoxTeamArea_team_item__3w5mq")).get(0);
                String homeTeam = homeTeamElement.findElement(By.className("MatchBoxTeamArea_team__3aB4O")).getText();
                String homeTeamScore = getTeamScore(homeTeamElement);

                // 원정팀 정보
                WebElement awayTeamElement = matchItem.findElements(By.className("MatchBoxTeamArea_team_item__3w5mq")).get(1);
                String awayTeam = awayTeamElement.findElement(By.className("MatchBoxTeamArea_team__3aB4O")).getText();
                String awayTeamScore = getTeamScore(awayTeamElement);

                // GameSchedule 객체 생성 및 리스트에 추가
                GameSchedule gameSchedule = new GameSchedule(startDate, stadium, homeTeam, awayTeam, homeTeamScore, awayTeamScore);
                matchList.add(gameSchedule);
            }
        } catch (Exception e) {
            e.printStackTrace(); // 오류 발생 시 스택 트레이스 출력
        } finally {
            driver.quit(); // 브라우저 종료
        }
        return matchList;
    }

    private static String getTeamScore(WebElement teamElement) {
        try {
            return teamElement.findElement(By.className("MatchBoxTeamArea_score__1_YFB")).getText();
        } catch (Exception e) {
            return "N/A"; // 스코어가 없는 경우
        }
    }
}