package com.atguigu.gmall.gateway.filters;

import com.atguigu.gmall.common.utils.IpUtils;
import com.atguigu.gmall.common.utils.JwtUtils;
import com.atguigu.gmall.gateway.config.JwtProperties;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.cloud.gateway.filter.factory.GatewayFilterFactory;
import org.springframework.http.HttpCookie;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.MultiValueMap;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

@EnableConfigurationProperties(JwtProperties.class)
@Component
public class AuthGatewayFilterFactory extends AbstractGatewayFilterFactory<AuthGatewayFilterFactory.PathConfig> {
    @Autowired
    private JwtProperties properties;

    @Override
    public GatewayFilter apply(PathConfig config) {
        return new GatewayFilter() {
            @Override
            public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
                //serverhttprequest->httpservletrequest
                ServerHttpRequest request = exchange.getRequest();
                ServerHttpResponse response = exchange.getResponse();
                //判断请求在不在拦截名单中,不在方形
                List<String> pathes = config.pathes;
                String curPath = request.getURI().getPath();
                if (!CollectionUtils.isEmpty(pathes)){
                    if (!pathes.stream().anyMatch(path->curPath.startsWith(path))){
                        return chain.filter(exchange);

                    }
                }


                //获取token信息,同步-cookie异步-头信息
                //一步情况下头信息获取
                HttpHeaders headers = request.getHeaders();
                String token = headers.getFirst("token");
                //如果头信息没有token信息则尝试从cookie中获取token信息
                if (StringUtils.isBlank(token)){
                    MultiValueMap<String, HttpCookie> cookies = request.getCookies();
                    if (!CollectionUtils.isEmpty(cookies)&&cookies.containsKey(properties.getCookieName())){
                        HttpCookie cookie = cookies.getFirst(properties.getCookieName());
                        token = cookie.getValue();
                    }
                }
                //判断token是否为空,为空重定向到登录页面冰蓝姐
                if (StringUtils.isBlank(token)){
                    response.setStatusCode(HttpStatus.SEE_OTHER);
                    response.getHeaders().set(HttpHeaders.LOCATION,"http://sso.gmall.com/toLogin.html?returnUrl="+request.getURI());
                    return response.setComplete();
                }
                //解析token信息,解析异常重定向到登录页面冰蓝姐
                try {
                    Map<String, Object> map = JwtUtils.getInfoFromToken(token, properties.getPublicKey());
                    //判断token中的IP和当前请求的IP是否一致
                    String ip = map.get("ip").toString();
                    String curIp = IpUtils.getIpAddressAtGateway(request);
                    if (!StringUtils.equals(ip,curIp)){
                        response.setStatusCode(HttpStatus.SEE_OTHER);
                        response.getHeaders().set(HttpHeaders.LOCATION,"http://sso.gmall.com/toLogin.html?returnUrl="+request.getURI());
                        return response.setComplete();
                    }

                    //把解析后的登录信息传递给后续服务

                    request.mutate().header("userId",map.get("userId").toString()).build();
                    //方形
                    exchange.mutate().request(request).build();
                    return chain.filter(exchange);
                } catch (Exception e) {
                    e.printStackTrace();
                    response.setStatusCode(HttpStatus.SEE_OTHER);
                    response.getHeaders().set(HttpHeaders.LOCATION,"http://sso.gmall.com/toLogin.html?returnUrl="+request.getURI());
                    return response.setComplete();

                }
            }
        };
    }

    public AuthGatewayFilterFactory() {
        super(PathConfig.class);
    }

//    @Data
//    public static class KeyValueConfig{
//            private String key;
//            private String value;
//            private String desc;
//    }
    @Data
    public static class PathConfig{
        private List<String> pathes;
}

    @Override
    public List<String> shortcutFieldOrder() {
        return Arrays.asList("pathes");
    }

    @Override
    public ShortcutType shortcutType() {
        return ShortcutType.GATHER_LIST;
    }
}
