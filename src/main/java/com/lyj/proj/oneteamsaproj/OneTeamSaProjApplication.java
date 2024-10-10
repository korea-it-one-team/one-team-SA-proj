package com.lyj.proj.oneteamsaproj;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

@SpringBootApplication(exclude={DataSourceAutoConfiguration.class})
public class OneTeamSaProjApplication {

    public static void main(String[] args) {
        SpringApplication.run(OneTeamSaProjApplication.class, args);
    }

}
