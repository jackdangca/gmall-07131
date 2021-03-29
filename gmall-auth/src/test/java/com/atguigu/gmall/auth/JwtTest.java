package com.atguigu.gmall.auth;

import com.atguigu.gmall.common.utils.JwtUtils;
import com.atguigu.gmall.common.utils.RsaUtils;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;


import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.HashMap;
import java.util.Map;

public class JwtTest {

    // 别忘了创建D:\\project\rsa目录
	private static final String pubKeyPath = "D:\\Users\\IdeaProjects\\rsa\\rsa.pub";
    private static final String priKeyPath = "D:\\Users\\IdeaProjects\\rsa\\rsa.pri";

    private PublicKey publicKey;

    private PrivateKey privateKey;

    @Test
    public void testRsa() throws Exception {
        RsaUtils.generateKey(pubKeyPath, priKeyPath, "234");
    }

    @BeforeEach
    public void testGetRsa() throws Exception {
        this.publicKey = RsaUtils.getPublicKey(pubKeyPath);
        this.privateKey = RsaUtils.getPrivateKey(priKeyPath);
    }

    @Test
    public void testGenerateToken() throws Exception {
        Map<String, Object> map = new HashMap<>();
        map.put("id", "11");
        map.put("username", "liuyan");
        // 生成token
        String token = JwtUtils.generateToken(map, privateKey, 5);
        System.out.println("token = " + token);
    }

    @Test
    public void testParseToken() throws Exception {
        String token = "eyJhbGciOiJSUzI1NiJ9.eyJpZCI6IjExIiwidXNlcm5hbWUiOiJsaXV5YW4iLCJleHAiOjE2MTYwNDg2NjJ9.jjJzmr7-CZd8w2iMeRLUIn6s39KmIenR10yBZJ2V15KKsilUCN4Fe6zr_c-Ywb75WO8KjIKrprDr7AxEtW3g0jVzQxy9y2mJo05cECxlNL_sWk-uVHZUynuOx0HeHXnTVZNM_IrHSz60eLkKeantM82ciAqfjSFCq7i-eUstsE3_eVtblNC15z0JNfDr8dFqaCSeVFc22Aib7_YEz2pKA-_9x-WxPK2TChSZjlJEMRN4DulqH5nZvogd7fFXQYKxh4Sn2UutJLWkNJCh1yWNdo8IPfyH0kszG6N_USqFXV1T5vXWGzaFy9sazQ9vpYhkdVh_nK67-MMIgCoUo5CcTA\n";

        // 解析token
        Map<String, Object> map = JwtUtils.getInfoFromToken(token, publicKey);
        System.out.println("id: " + map.get("id"));
        System.out.println("userName: " + map.get("username"));
    }
}