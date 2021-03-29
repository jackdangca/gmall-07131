package com.atguigu.gmall.auth.service;

import com.atguigu.gmall.auth.config.JwtProperties;
import com.atguigu.gmall.auth.feign.GmallUmsClient;
import com.atguigu.gmall.common.bean.ResponseVo;
import com.atguigu.gmall.common.exception.AuthException;
import com.atguigu.gmall.common.utils.CookieUtils;
import com.atguigu.gmall.common.utils.IpUtils;
import com.atguigu.gmall.common.utils.JwtUtils;
import com.atguigu.gmall.ums.entity.UserEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Service;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;
@EnableConfigurationProperties(JwtProperties.class)
@Service
public class AuthService {
    @Autowired
    private GmallUmsClient umsClient;
    @Autowired
    private JwtProperties properties;

    public void login(String loginName, String password, HttpServletRequest request, HttpServletResponse response) {
        //远程调用接口
        ResponseVo<UserEntity> userEntityResponseVo = this.umsClient.queryUser(loginName, password);
        UserEntity userEntity = userEntityResponseVo.getData();
        //判断用户信息是否为空,为空跑一场
        if (userEntity==null) {
            throw  new AuthException("用户名或者密码错误");

        }
        //组装在和信息
        try {
            Map<String, Object> map = new HashMap<>();
            map.put("userId",userEntity.getId());
            map.put("username",userEntity.getUsername());
            String ip = IpUtils.getIpAddressAtService(request);
            map.put("ip",ip);
            //生成jwt类型的token信息,为了防止被盗用,要加入用户的IP地址
            String token = JwtUtils.generateToken(map, this.properties.getPrivateKey(), this.properties.getExpire());
            //放入cookie中
            CookieUtils.setCookie(request,response,properties.getCookieName(),token,properties.getExpire()*60);
            //把用户昵称放入cookie中
            CookieUtils.setCookie(request,response,this.properties.getUnick(),userEntity.getNickname(),this.properties.getExpire()*60);

        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
