package com.atguigu.scheduled.demo.juc;

import java.util.concurrent.DelayQueue;
import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;

public class DelayQueueDemo {
    public static void main(String[] args) {
        System.out.println("定时任务初始化时间"+System.currentTimeMillis());
        new DelayTask().scheduledAtFixed(()->{
            System.out.println("定时任务执行时间"+System.currentTimeMillis());
        },5,10,TimeUnit.SECONDS);
//        new DelayTask().scheduledAtFixed();

    }

}
class DelayTask implements Delayed{
    //一直调用该方法,方法返回值小于等于0,说明元素可以出对
    private DelayQueue<DelayTask>queue = new DelayQueue<>();
    private Long time;

    @Override
    public long getDelay(TimeUnit unit) {
        return time-System.currentTimeMillis();
    }
    //派对方法,
    @Override
    public int compareTo(Delayed o) {
        return (int)(this.time -((DelayTask) o).time);
    }
    public void scheduled(Runnable runnable,int delay,TimeUnit timeUnit){
        try {
            this.time = System.currentTimeMillis() + timeUnit.toMillis(delay);
            queue.put(this);
            queue.take();
            new Thread(runnable).start();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }
    public void scheduledAtFixed(Runnable runnable,int delay,int period,TimeUnit timeUnit){
        try {
            this.time = System.currentTimeMillis() + timeUnit.toMillis(delay);
            int flag =1;
            while (true){
                if (flag!=1){
                    this.time+=timeUnit.toMillis(period);
                }
                queue.put(this);
                queue.take();
                new Thread(runnable).start();
                flag++;
            }

        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}