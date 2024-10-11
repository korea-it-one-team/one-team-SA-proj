package com.lyj.proj.oneteamsaproj.crawl;

import com.lyj.proj.oneteamsaproj.vo.News;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class NewsCrawl {
    public List<News> crawl() {
        List<News> newsList = new ArrayList<>(); // 뉴스 정보를 저장할 리스트

        System.setProperty("webdriver.chrome.driver", "C:/work_oneteam/one-team-SA-proj/chromedriver-win64/chromedriver.exe");

        ChromeOptions options = new ChromeOptions();
        // options.addArguments("--headless"); // 필요 시 주석 해제
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");

        WebDriver driver = new ChromeDriver(options);

        try {
            driver.get("https://sports.news.naver.com/wfootball/news/index?isphoto=N");

            // 최신 뉴스 항목을 가져오는 부분
            List<WebElement> newsItems;
            do {
                // 현재 뉴스 항목을 가져오기
                newsItems = driver.findElements(By.cssSelector("div#_newsList ul li"));

                // 뉴스 항목이 존재하는지 확인
                for (WebElement item : newsItems) {
                    try {
                        // 뉴스 제목과 링크 추출
                        String title = item.findElement(By.cssSelector("div.text a.title span")).getText(); // 제목
                        String link = item.findElement(By.cssSelector("div.text a.title")).getAttribute("href"); // 링크

                        // 이미지 URL 가져오기 (없을 경우 빈 문자열로 처리)
                        String imgUrl = getImageUrl(item);

                        // 요약 텍스트, 언론사, 시간 정보 추출
                        String desc = item.findElement(By.cssSelector("div.text p.desc")).getText(); // 요약 텍스트
                        String press = item.findElement(By.cssSelector("div.source span.press")).getText(); // 언론사
                        String time = getTime(item); // 시간 정보

                        // 뉴스 객체 생성 후 리스트에 추가
                        newsList.add(new News(title, link, imgUrl, desc, press, time));

                        // 5개가 모이면 더 이상 크롤링하지 않음
                        if (newsList.size() >= 5) {
                            break; // 반복문을 빠져나가서 더 이상 뉴스 수집하지 않도록 함
                        }
                    } catch (StaleElementReferenceException e) {
                        // 요소를 다시 가져오기
                        newsItems = driver.findElements(By.cssSelector("div#_newsList ul li"));
                    }
                }

            } while (!newsItems.isEmpty() && newsList.size() < 5); // 뉴스 항목이 비어있지 않고, 수집된 뉴스가 5개 미만인 경우 계속

        } catch (Exception e) {
            e.printStackTrace(); // 오류 발생 시 스택 트레이스 출력
        } finally {
            driver.quit(); // 브라우저 종료
        }
        return newsList; // 크롤링한 뉴스 리스트 반환
    }

    private String getImageUrl(WebElement item) {
        try {
            return item.findElement(By.cssSelector("a.thmb img")).getAttribute("src"); // 이미지 URL
        } catch (NoSuchElementException e) {
            return ""; // 이미지가 없을 경우 빈 문자열로 설정됨
        }
    }

    private String getTime(WebElement item) {
        try {
            return item.findElement(By.cssSelector("div.source span.time")).getText(); // 시간
        } catch (NoSuchElementException e) {
            return ""; // 시간이 없을 경우 빈 문자열로 설정됨
        }
    }
}



