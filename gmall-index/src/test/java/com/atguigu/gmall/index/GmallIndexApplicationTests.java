package com.atguigu.gmall.index;

import com.google.common.base.Charsets;
import com.google.common.hash.BloomFilter;
import com.google.common.hash.Funnels;
import org.junit.jupiter.api.Test;
import org.redisson.api.RBloomFilter;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.nio.charset.Charset;

@SpringBootTest
class GmallIndexApplicationTests {
    @Autowired
    private StringRedisTemplate redisTemplate;
    @Autowired
    RedissonClient redissonClient;

    @Test
    void contextLoads() {
        RBloomFilter<Object> bloomFilter = this.redissonClient.getBloomFilter("bloomFilter");
        bloomFilter.tryInit(10,0.3);
        bloomFilter.add("1");
        bloomFilter.add("2");
        bloomFilter.add("3");
        System.out.println(bloomFilter.contains("1"));
        System.out.println(bloomFilter.contains("2"));
        System.out.println(bloomFilter.contains("3"));
        System.out.println(bloomFilter.contains("4"));
        System.out.println(bloomFilter.contains("5"));
        System.out.println(bloomFilter.contains("6"));
        System.out.println(bloomFilter.contains("7"));
        System.out.println(bloomFilter.contains("8"));
        System.out.println(bloomFilter.contains("9"));
        System.out.println(bloomFilter.contains("10"));
        System.out.println(bloomFilter.contains("11"));
//        this.redisTemplate.opsForValue().set("username","liuyan");
    }

//    public static void main(String[] args) {
//        BloomFilter<CharSequence> bloomFilter = BloomFilter.create(Funnels.stringFunnel(Charsets.UTF_8), 10, 0.3);
//        bloomFilter.put("1");
//        bloomFilter.put("2");
//        bloomFilter.put("3");
//        bloomFilter.put("4");
//        bloomFilter.put("5");
//        System.out.println(bloomFilter.mightContain("1"));
//        System.out.println(bloomFilter.mightContain("2"));
//        System.out.println(bloomFilter.mightContain("3"));
//        System.out.println(bloomFilter.mightContain("6"));
//        System.out.println(bloomFilter.mightContain("7"));
//        System.out.println(bloomFilter.mightContain("8"));
//        System.out.println(bloomFilter.mightContain("9"));
//        System.out.println(bloomFilter.mightContain("10"));
//        System.out.println(bloomFilter.mightContain("11"));
//
//
//    }

}
