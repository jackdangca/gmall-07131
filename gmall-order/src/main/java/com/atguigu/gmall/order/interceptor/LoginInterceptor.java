package com.atguigu.gmall.order.interceptor;

import com.atguigu.gmall.cart.pojo.UserInfo;
import com.atguigu.gmall.common.utils.CookieUtils;
import com.atguigu.gmall.common.utils.JwtUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;
import java.util.UUID;


@Component
public class LoginInterceptor implements HandlerInterceptor {


//    public static String userId;
    private static final ThreadLocal<UserInfo>THREAD_LOCAL = new ThreadLocal<>();
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
//        userId="123456";
//        request.setAttribute("userId",userId);
//        System.out.println("这是拦截器的前值方法");
        //获取登录状态
//        UserInfo userInfo = new UserInfo();
//        userInfo.setUserKey("12121212");
//        userInfo.setUserId(12l);
//        THREAD_LOCAL.set(userInfo);
        UserInfo userInfo = new UserInfo();

        Long userId = Long.valueOf(request.getHeader("userId"));
        userInfo.setUserId(userId);
        THREAD_LOCAL.set(userInfo);

        return true;
    }
    public static UserInfo getUserInfo(){
        return THREAD_LOCAL.get();
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        //一定要手动清理threadlocal中的线程局部变量,因为使用的是tomcat线程池,请求结束线程没有结束,否则容易产生内存泄漏
        THREAD_LOCAL.remove();
    }
}
