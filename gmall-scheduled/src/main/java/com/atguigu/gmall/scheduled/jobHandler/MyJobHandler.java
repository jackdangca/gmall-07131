package com.atguigu.gmall.scheduled.jobHandler;

import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.scheduled.mapper.CartMapper;
import com.atguigu.gmall.scheduled.pojo.Cart;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.handler.annotation.XxlJob;
import com.xxl.job.core.log.XxlJobLogger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.BoundSetOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class MyJobHandler {
    @Autowired
    StringRedisTemplate redisTemplate;
    @Autowired
    private CartMapper cartMapper;
    private static final String exception_key="cart:exception:userId";
    private static final String key_prefix = "cart:info:";

    @XxlJob("myJobHandler")
    public ReturnT<String> test(String param){
        System.out.println("任务执行时间"+System.currentTimeMillis()+param);
        XxlJobLogger.log("myjobhandler executed"+param);

        return ReturnT.SUCCESS;
    }
    @XxlJob("cartDataSyncJobHandler")
    public ReturnT<String> dataSync(String param){
        BoundSetOperations<String, String> setOps = this.redisTemplate.boundSetOps(exception_key);
        String userId = setOps.pop();
        while (userId!=null){
            //全部删除失败用mysql中购物车
            this.cartMapper.delete(new UpdateWrapper<Cart>().eq("user_id",userId));
            //读取redis中失败用户的购物车记录,通不过去
            if (!this.redisTemplate.hasKey(key_prefix+userId)) {
                return ReturnT.SUCCESS;

            }
            BoundHashOperations<String, Object, Object> hashOps = this.redisTemplate.boundHashOps(key_prefix + userId);
            List<Object> cartJsons = hashOps.values();
            cartJsons.forEach(cartJson->{
                Cart cart = JSON.parseObject(cartJson.toString(),Cart.class);
                this.cartMapper.insert(cart);
            });

            //新增Redis中的购物车记录

            userId = setOps.pop();

        }
        return ReturnT.SUCCESS;



    }
}
