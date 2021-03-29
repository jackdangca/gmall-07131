package com.atguigu.gmall.index.service;

import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.common.bean.ResponseVo;
import com.atguigu.gmall.index.aspect.GmallCache;
import com.atguigu.gmall.index.feign.GmallPmsClient;
import com.atguigu.gmall.index.tools.DistributedLock;
import com.atguigu.gmall.pms.entity.CategoryEntity;
import jdk.nashorn.internal.ir.IfNode;
import org.apache.commons.lang.StringUtils;
import org.redisson.api.RCountDownLatch;
import org.redisson.api.RLock;
import org.redisson.api.RReadWriteLock;
import org.redisson.api.RedissonClient;
import org.redisson.client.protocol.Time;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
public class IndexService {
    @Autowired
    GmallPmsClient pmsClient;
    @Autowired
    StringRedisTemplate redisTemplate;
    @Autowired
    DistributedLock distributedLock;
    @Autowired
    RedissonClient redissonClient;
    private  static  final  String key_prefix="index:cates";
    public List<CategoryEntity>queryLv1CategoriesByPid(){
        ResponseVo<List<CategoryEntity>> listResponseVo = this.pmsClient.queryCategoriesByPid(0l);
        List<CategoryEntity> categoryEntities = listResponseVo.getData();
        return categoryEntities;
    }

    public List<CategoryEntity> queryLv12CategoriesWithSubByPid2(Long pid) {
        String json = this.redisTemplate.opsForValue().get(key_prefix + pid);
        if (StringUtils.isNotBlank(json)&&!StringUtils.equals("null",json)){
            return JSON.parseArray(json,CategoryEntity.class);
        }else if (StringUtils.equals("null",json)){
            return null;
        }
        RLock lock = this.redissonClient.getFairLock("index:lock" + pid);
        lock.lock();
        //在获取所过程中可能有其他请求提前获取到锁,再查一遍
        String json2 = this.redisTemplate.opsForValue().get(key_prefix + pid);
        if (StringUtils.isNotBlank(json2)&&!StringUtils.equals("null",json2)){
            return JSON.parseArray(json2,CategoryEntity.class);
        }else if (StringUtils.equals("null",json2)){
            return null;
        }


        ResponseVo<List<CategoryEntity>> listResponseVo = this.pmsClient.queryCategoriesWithSubsByPid(pid);
        List<CategoryEntity> data = listResponseVo.getData();
        if (CollectionUtils.isEmpty(data)){
            this.redisTemplate.opsForValue().set(key_prefix+pid,null,3,TimeUnit.MINUTES);
        }else {
            this.redisTemplate.opsForValue().set(key_prefix+pid,JSON.toJSONString(data),30+ new Random().nextInt(10), TimeUnit.DAYS);


        }




        return data;
    }
    @GmallCache(prefix = key_prefix,timeout = 43200,random = 4320,lock = "index:lock")
    public List<CategoryEntity> queryLv12CategoriesWithSubByPid(Long pid) {
        ResponseVo<List<CategoryEntity>> listResponseVo = this.pmsClient.queryCategoriesWithSubsByPid(pid);
        List<CategoryEntity> data = listResponseVo.getData();
        return data;
    }
    public   void  testLock2() {
        String uuid = UUID.randomUUID().toString();
        Boolean flag = this.redisTemplate.opsForValue().setIfAbsent("lock", uuid,3,TimeUnit.SECONDS);
        if (!flag){
            try {
                Thread.sleep(100);
                testLock();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }else {
//            this.redisTemplate.expire("lock",3,TimeUnit.SECONDS);

            String number = this.redisTemplate.opsForValue().get("number");
            if (StringUtils.isBlank(number)){
                return;
            }
            int i = Integer.parseInt(number);
            this.redisTemplate.opsForValue().set("number",String.valueOf(++i));
            String script = "if(redis.call('get',KEYS[1])==ARGV[1]) then return redis.call('del','lock')else return 0 end";
            this.redisTemplate.execute(new DefaultRedisScript<>(script,Boolean.class), Arrays.asList("lock"),uuid);
//            if (StringUtils.equals(uuid,this.redisTemplate.opsForValue().get("lock"))){
//                this.redisTemplate.delete("lock");
//
//            }

        }


    }
    public   void  testLock3() {
        RLock lock = this.redissonClient.getLock("lock");
        lock.lock();

        String number = this.redisTemplate.opsForValue().get("number");
            if (StringUtils.isBlank(number)){
                return;
            }
            int i = Integer.parseInt(number);
            this.redisTemplate.opsForValue().set("number",String.valueOf(++i));
            lock.unlock();





    }
    public void testSublock(String uuid){
        this.distributedLock.tryLock("lock",uuid,30);
        System.out.println("测试可重入锁");
        this.distributedLock.unlock("lock",uuid);
    }
    public   void  testLock() {
        String uuid = UUID.randomUUID().toString();
        Boolean flag = this.distributedLock.tryLock("lock", uuid, 30);
        if (flag){


            String number = this.redisTemplate.opsForValue().get("number");
            if (StringUtils.isBlank(number)){
                return;
            }
            int i = Integer.parseInt(number);
            this.redisTemplate.opsForValue().set("number",String.valueOf(++i));
//            this.testSublock(uuid);
            try {
                TimeUnit.SECONDS.sleep(90);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            this.distributedLock.unlock("lock",uuid);

//            if (StringUtils.equals(uuid,this.redisTemplate.opsForValue().get("lock"))){
//                this.redisTemplate.delete("lock");
//
//            }

        }


    }

    public void testWrite() {
        RReadWriteLock rwlock = this.redissonClient.getReadWriteLock("rwlock");
        rwlock.writeLock().lock(10,TimeUnit.SECONDS);
        System.out.println("=================");
    }

    public void testRead() {
        RReadWriteLock rwLock = this.redissonClient.getReadWriteLock("rwlock");
        rwLock.readLock().lock(10, TimeUnit.SECONDS);
        System.out.println("==================");
    }

    public void testLatch() {
        RCountDownLatch latch = this.redissonClient.getCountDownLatch("latch");
        latch.trySetCount(6);
        try {
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void testCountDown() {
        RCountDownLatch latch = this.redissonClient.getCountDownLatch("latch");
        latch.countDown();

    }
}
