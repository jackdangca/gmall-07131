package com.atguigu.gmall.wms.service.impl;

import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.common.exception.OrderException;
import com.atguigu.gmall.wms.vo.SkuLockVo;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.gmall.common.bean.PageResultVo;
import com.atguigu.gmall.common.bean.PageParamVo;

import com.atguigu.gmall.wms.mapper.WareSkuMapper;
import com.atguigu.gmall.wms.entity.WareSkuEntity;
import com.atguigu.gmall.wms.service.WareSkuService;
import org.springframework.util.CollectionUtils;


@Service("wareSkuService")
public class WareSkuServiceImpl extends ServiceImpl<WareSkuMapper, WareSkuEntity> implements WareSkuService {
    @Autowired
    RedissonClient redissonClient;
    @Autowired
    WareSkuMapper wareSkuMapper;
    @Autowired
    StringRedisTemplate redisTemplate;
    private static  final String key_prefix="stock:lock:";

    @Override
    public PageResultVo queryPage(PageParamVo paramVo) {
        IPage<WareSkuEntity> page = this.page(
                paramVo.getPage(),
                new QueryWrapper<WareSkuEntity>()
        );

        return new PageResultVo(page);
    }

    @Override
    public List<SkuLockVo> checkAndLock(List<SkuLockVo> lockVos,String ordeToken) {
        if (CollectionUtils.isEmpty(lockVos)) {
            throw new OrderException("没有选中的商品,请去购物车中选中要购买的商品");
        }
        lockVos.forEach(lockVo -> {
            checkLock(lockVo);

        });
        //判断是否所有商品锁定成功,如果有一个锁定失败,全部解锁
        if (lockVos.stream().anyMatch(lockVo -> !lockVo.getLock())){
            List<SkuLockVo> successLockVos = lockVos.stream().filter(SkuLockVo::getLock).collect(Collectors.toList());
            if (!CollectionUtils.isEmpty(successLockVos)){
                successLockVos.forEach(lockVo -> {
                    this.wareSkuMapper.unLock(lockVo.getWareSkuId(),lockVo.getCount());



            });
        }
            //有商品锁定失败的情况下,需要吧锁定信息返回给消费方
            return lockVos;
        }
        //所有商品锁定成功的情况下,返回前,应该吧锁定信息缓存到redis中,一遍将来某个时刻解锁对应的库存
        this.redisTemplate.opsForValue().set(key_prefix+ordeToken, JSON.toJSONString(lockVos));
        //如果所有商品都锁定成功,返回null
        return null;
    }
    private void checkLock(SkuLockVo lockVo){
        RLock fairLock = this.redissonClient.getFairLock("stock:lock:" + lockVo.getSkuId());
        try {
            fairLock.lock();
            //盐库存
            List<WareSkuEntity> wareSkuEntities = this.wareSkuMapper.check(lockVo.getSkuId(), lockVo.getCount());
            if (CollectionUtils.isEmpty(wareSkuEntities)){
                lockVo.setLock(false);
                return;
            }

            //锁库存
            //去第一个视频
            Long id = wareSkuEntities.get(0).getId();
            if (this.wareSkuMapper.lock(id,lockVo.getCount())==1) {
                lockVo.setLock(true);
                lockVo.setWareSkuId(id);
                return;
            }

        } finally {
            fairLock.unlock();
        }
    }

}