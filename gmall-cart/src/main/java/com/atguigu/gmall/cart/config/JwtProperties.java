package com.atguigu.gmall.cart.config;
import com.atguigu.gmall.common.utils.RsaUtils;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import javax.annotation.PostConstruct;
import java.io.File;
import java.security.PrivateKey;
import java.security.PublicKey;

@Data
//将来使用时通过注解enableconfigrationproperties(jwtproperties.class)
@ConfigurationProperties(prefix = "auth.jwt")
public class JwtProperties {
    private String pubKeyPath;
    private String cookieName;
    private PublicKey publicKey;
    private String userKey;
    private Integer expire;
    @PostConstruct
    public void init(){
        try {
            File pubFile = new File(pubKeyPath);


            this.publicKey=RsaUtils.getPublicKey(pubKeyPath);

        } catch (Exception e) {
            e.printStackTrace();
        }


    }


}
