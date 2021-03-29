package com.atguigu.scheduled.demo.jdk;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class TimerDemo {
    public static void main(String[] args) {
        System.out.println("任务初始化时间"+System.currentTimeMillis());
//        new Timer().schedule(new TimerTask() {
//            @Override
//            public void run() {
//                System.out.println("任务执行时间"+System.currentTimeMillis());
//            }
//        },5000,10000);
//    }
        ScheduledExecutorService executorService = Executors.newScheduledThreadPool(3);
        executorService.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
             System.out.println("任务执行时间"+System.currentTimeMillis());

            }
        },5,10, TimeUnit.SECONDS);
    }
}
