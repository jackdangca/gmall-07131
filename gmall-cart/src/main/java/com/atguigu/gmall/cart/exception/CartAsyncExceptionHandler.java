package com.atguigu.gmall.cart.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundSetOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.Arrays;

@Slf4j
@Component
public class CartAsyncExceptionHandler implements AsyncUncaughtExceptionHandler {
    @Autowired
    StringRedisTemplate redisTemplate;
    private static final String exception_key="cart:exception:userId";


    @Override
    public void handleUncaughtException(Throwable throwable, Method method, Object... objects) {
        log.error("你的异步任务出现异常了.方法: {},参数: {},异常信息 {}",method.getName(), Arrays.asList(objects),throwable.getMessage());
        String userId = objects[0].toString();
        BoundSetOperations<String, String> setOps = this.redisTemplate.boundSetOps(exception_key);
        setOps.add(userId);

    }

}
