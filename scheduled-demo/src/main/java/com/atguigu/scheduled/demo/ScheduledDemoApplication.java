package com.atguigu.scheduled.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class ScheduledDemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(ScheduledDemoApplication.class, args);
    }

}
