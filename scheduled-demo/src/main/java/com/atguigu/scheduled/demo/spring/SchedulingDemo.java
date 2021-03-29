package com.atguigu.scheduled.demo.spring;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.Date;

@Component
public class SchedulingDemo {
//    @Scheduled(fixedDelay = 5000,initialDelay = 10000)
    @Scheduled(cron = "0/10 * * * * *")
    public void test(){

        System.out.println(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));

    }
}
