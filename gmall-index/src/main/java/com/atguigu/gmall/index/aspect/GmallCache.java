package com.atguigu.gmall.index.aspect;

import org.springframework.transaction.annotation.Transactional;

import java.lang.annotation.*;

@Target({ ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
public @interface GmallCache {
    /*
    * 缓存的前缀
    * key:prefix+:+方法参数*/
    String prefix() default "";
    /*
    * 缓存默认时间,单位是分钟*/
    int timeout() default 5;
//    防止缓存血崩
    int random() default 5;
//    为了防止缓存击穿制定分布式的key,锁的前缀
    String lock() default "lock";
}
