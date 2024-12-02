package com.lyj.proj.oneteamsaproj.scheduler;

import com.lyj.proj.oneteamsaproj.service.GameScheduleService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

// AWS 배포했을때 자정마다 알아서 경기일정 크롤링해오는 클래스(스케줄러)
@Component
public class GameScheduleBatch {

    private final GameScheduleService gameScheduleService;

    public GameScheduleBatch(GameScheduleService gameScheduleService) {
        this.gameScheduleService = gameScheduleService;
    }

    // 하루에 한 번 실행 (00:00)
    @Scheduled(cron = "0 0 0 * * *")
    public void crawlAndSaveSchedule() {
        gameScheduleService.crawlAndSaveGameSchedules();
    }
}
