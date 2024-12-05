package com.lyj.proj.oneteamsaproj;

import io.github.cdimascio.dotenv.Dotenv;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;

@EnableScheduling // 회원 탈퇴 유예기간 지나면 스케줄러에서 관리하기 때문에 추가
@SpringBootApplication
@MapperScan("com.lyj.proj.oneteamsaproj.repository")
public class OneTeamSaProjApplication {

    public static void main(String[] args) {

        Dotenv dotenv = Dotenv.load();

        // Dotenv로 로드한 값을 시스템 환경 변수로 설정
        System.setProperty("API_KEY", dotenv.get("API_KEY"));
        System.setProperty("API_SECRETKEY", dotenv.get("API_SECRETKEY"));
        System.setProperty("BUCKET_KEY", dotenv.get("BUCKET_KEY"));
        System.setProperty("CLASSPATH", dotenv.get("CLASSPATH"));
        System.setProperty("PROJECT_ID", dotenv.get("PROJECT_ID"));

        // GOOGLE_APPLICATION_CREDENTIALS 환경 변수 설정
        String googleCredentialsPath = dotenv.get("GOOGLE_APPLICATION_CREDENTIALS");
        if (googleCredentialsPath != null && !googleCredentialsPath.isEmpty()) {
            System.setProperty("GOOGLE_APPLICATION_CREDENTIALS", googleCredentialsPath);
        } else {
            System.err.println("GOOGLE_APPLICATION_CREDENTIALS 환경 변수가 설정되지 않았습니다.");
        }

        // GOOGLE_APPLICATION_CREDENTIALS 값 확인
        String googleAppCredentials = System.getProperty("GOOGLE_APPLICATION_CREDENTIALS");
        System.out.println("설정된 GOOGLE_APPLICATION_CREDENTIALS: " + googleAppCredentials);

        SpringApplication.run(OneTeamSaProjApplication.class, args);

        // Shutdown Hook 추가
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("Spring Boot 강제 종료 감지. Flask 서버를 종료합니다.");
            try {
                // Flask 서버에 종료 요청 (타임아웃 추가)
                URL url = new URL("http://localhost:5000/shutdown");
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setConnectTimeout(5000);  // 연결 타임아웃 5초
                connection.setReadTimeout(5000);     // 읽기 타임아웃 5초
                int responseCode = connection.getResponseCode();

                if (responseCode == 200) {
                    System.out.println("Flask 서버가 정상적으로 종료되었습니다.");
                } else {
                    System.err.println("Flask 서버 종료 요청 실패. 응답 코드: " + responseCode);
                }
            } catch (SocketTimeoutException e) {
                System.err.println("Flask 서버 종료 요청이 타임아웃되었습니다.");
            } catch (IOException e) {
                System.err.println("Flask 서버 종료 요청 중 연결 문제가 발생했습니다: " + e.getMessage());
            }
        }));
    }
}
