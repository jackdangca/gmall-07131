package com.atguigu.gmall.cart.service;

import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.cart.feign.GmallPmsClient;
import com.atguigu.gmall.cart.feign.GmallSmsClient;
import com.atguigu.gmall.cart.feign.GmallWmsClient;
import com.atguigu.gmall.cart.interceptor.LoginInterceptor;
import com.atguigu.gmall.cart.mapper.CartMapper;
import com.atguigu.gmall.cart.pojo.Cart;
import com.atguigu.gmall.cart.pojo.UserInfo;
import com.atguigu.gmall.common.bean.ResponseVo;
import com.atguigu.gmall.common.exception.CartException;
import com.atguigu.gmall.pms.entity.SkuAttrValueEntity;
import com.atguigu.gmall.pms.entity.SkuEntity;
import com.atguigu.gmall.sms.vo.ItemSaleVo;
import com.atguigu.gmall.wms.entity.WareSkuEntity;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.sun.org.apache.bcel.internal.generic.I2F;
import com.sun.org.apache.bcel.internal.generic.IfInstruction;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.concurrent.ListenableFuture;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class CartService {
    @Autowired
    private GmallPmsClient pmsClient;
    @Autowired
    private GmallWmsClient wmsClient;
    @Autowired
    private GmallSmsClient smsClient;
    @Autowired
    private StringRedisTemplate redisTemplate;
    @Autowired
    private CartMapper cartMapper;
    @Autowired
    private CartAsyncService cartAsyncService;
    private static final String key_prefix = "cart:info:";
    private static final String price_prefix = "cart:price:";
    public void addCart(Cart cart) {
        //获取登录状态
        String userId = getUserId();
        //组装外层的key
        String key = key_prefix + userId;
        //根据外层的key获取内层的map
        BoundHashOperations<String, Object, Object> hashOps = this.redisTemplate.boundHashOps(key);
        //判断该用户的用户车中是否包含该商品,
        BigDecimal count = cart.getCount();
        String skuId = cart.getSkuId().toString();
        if (hashOps.hasKey(skuId)) {
            // 包含责更新数量,
            String json = hashOps.get(skuId).toString();
            cart = JSON.parseObject(json, Cart.class);
            cart.setCount(cart.getCount().add(count));
            hashOps.put(skuId,JSON.toJSONString(cart));
            this.cartAsyncService.updateCartByUserIdAndSkuId(userId,skuId,cart);
        }else {
            //不包含责新增一条
            ResponseVo<SkuEntity> skuEntityResponseVo = this.pmsClient.querySkuById(cart.getSkuId());
            SkuEntity skuEntity = skuEntityResponseVo.getData();
            if (skuEntity==null) {
                throw new CartException("没有对应的商品,请检查");
            }
            cart.setUserId(userId);
            cart.setTitle(skuEntity.getTitle());
            cart.setCheck(true);
            cart.setPrice(skuEntity.getPrice());
            cart.setDefaultImage(skuEntity.getDefaultImage());
            ResponseVo<List<SkuAttrValueEntity>> saleAttrsResponseVo = this.pmsClient.querySaleAttrValueBySkuId(cart.getSkuId());
            List<SkuAttrValueEntity> skuAttrValueEntities = saleAttrsResponseVo.getData();
            cart.setSaleAttrs(JSON.toJSONString(skuAttrValueEntities));
            ResponseVo<List<ItemSaleVo>> salesResponseVo = this.smsClient.querySalesBySkuId(cart.getSkuId());
            List<ItemSaleVo> itemSaleVos = salesResponseVo.getData();
            cart.setSales(JSON.toJSONString(itemSaleVos));
            ResponseVo<List<WareSkuEntity>> wareResponseVo = this.wmsClient.queryWareSkusBySkuId(cart.getSkuId());
            List<WareSkuEntity> wareSkuEntities = wareResponseVo.getData();
            if (!CollectionUtils.isEmpty(wareSkuEntities)){
                cart.setStore(wareSkuEntities.stream().anyMatch(wareSkuEntity -> wareSkuEntity.getStock()-wareSkuEntity.getStockLocked()>0));
            }
            hashOps.put(skuId,JSON.toJSONString(cart));
            this.cartAsyncService.insertCart(userId,cart);
            //给购物车添加实时价格缓存
            this.redisTemplate.opsForValue().set(price_prefix+skuId,skuEntity.getPrice().toString());
        }




    }



    private String getUserId() {
        UserInfo userInfo = LoginInterceptor.getUserInfo();
        String userId = userInfo.getUserKey();
        if (userInfo.getUserId()!=null){
           userId =  userInfo.getUserId().toString();
        }
        return userId;
    }


    public Cart queryCartBySkuId(Long skuId) {
        String userId = this.getUserId();
        String key = key_prefix + userId;
        BoundHashOperations<String, Object, Object> hashOps = this.redisTemplate.boundHashOps(key);
        if (!hashOps.hasKey(skuId.toString())) {
            throw new CartException("没有对应的购物车记录");
        }
        String json = hashOps.get(skuId.toString()).toString();
        if (StringUtils.isNotBlank(json)){
            return JSON.parseObject(json,Cart.class);
        }
        throw new CartException("没有对应的购物车记录");


    }
    @Async
    public String executor1() throws InterruptedException {

            System.out.println("这是一个executor1方法,开始执行");
            TimeUnit.SECONDS.sleep(5);
            int i = 1/0;
            System.out.println("这是一个executor1方法,结束执行");

        return"hello executor1";

    }
    @Async
    public ListenableFuture<String> executor2(){
        try {
            System.out.println("这是一个executor2方法,开始执行");
            TimeUnit.SECONDS.sleep(5);
            System.out.println("这是一个executor2方法,结束执行");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return AsyncResult.forValue("hello executor2");

    }

    public List<Cart> queryCarts() {
        UserInfo userInfo = LoginInterceptor.getUserInfo();
        //先查未登录的购物车
        String userKey = userInfo.getUserKey();
        String unloginKey = key_prefix + userKey;
        BoundHashOperations<String, Object, Object> unloginHashOps = this.redisTemplate.boundHashOps(unloginKey);
        List<Object> unloginCartJsons = unloginHashOps.values();
        List<Cart> unloginCarts = null;
        if (!CollectionUtils.isEmpty(unloginCartJsons)) {
            unloginCarts = unloginCartJsons.stream().map(cartJson -> {
                Cart cart = JSON.parseObject(cartJson.toString(), Cart.class);
                String currentPrice = this.redisTemplate.opsForValue().get(price_prefix+cart.getSkuId());
                cart.setCurrentPrice(new BigDecimal(currentPrice));
                return cart;
            }).collect(Collectors.toList());
        }

        //获取登录状态,为登录则直接返回
        Long userId = userInfo.getUserId();
        if (userId == null) {
            return unloginCarts;
        }
        //登录,则合并未登录的购物车
        String loginKey = key_prefix + userId;
        BoundHashOperations<String, Object, Object> loginHashOps = this.redisTemplate.boundHashOps(loginKey);
        if (!CollectionUtils.isEmpty(unloginCarts)) {
            unloginCarts.forEach(cart -> {
                String skuId = cart.getSkuId().toString();
                BigDecimal count = cart.getCount();
                if (loginHashOps.hasKey(skuId)) {
                    String cartJson = loginHashOps.get(skuId).toString();
                    cart = JSON.parseObject(cartJson, Cart.class);
                    cart.setCount(cart.getCount().add(count));

                    loginHashOps.put(skuId, JSON.toJSONString(cart));
                    this.cartAsyncService.updateCartByUserIdAndSkuId(userId.toString(), skuId, cart);
                } else {
                    cart.setUserId(userId.toString());
                    loginHashOps.put(skuId, JSON.toJSONString(cart));
                    this.cartAsyncService.insertCart(userId.toString(),cart);

                }
            });
        }
        //删除未登录状态的购物车
        this.redisTemplate.delete(unloginKey);
        this.cartAsyncService.deleteByUserId(userKey);
        //查询登录状态的购物车,并返回
        List<Object> loginCartJsons = loginHashOps.values();
        if (CollectionUtils.isEmpty(loginCartJsons)){
            return null;
        }
        return loginCartJsons.stream().map(loginCartJson->{
            Cart cart = JSON.parseObject(loginCartJson.toString(), Cart.class);
            cart.setCurrentPrice(new BigDecimal(this.redisTemplate.opsForValue().get(price_prefix+cart.getSkuId())));
            return cart;
        }).collect(Collectors.toList());
    }

    public void updateNum(Cart cart) {
        String userId = this.getUserId();
        String key = key_prefix + userId;
        BoundHashOperations<String, Object, Object> hashOps = redisTemplate.boundHashOps(key);
        if (!hashOps.hasKey(cart.getSkuId().toString())){
            throw new CartException("该用户没有对应的购物车记录");
        }
        BigDecimal count = cart.getCount();
        String json = hashOps.get(cart.getSkuId().toString()).toString();
        cart = JSON.parseObject(json, Cart.class);
        cart.setCount(count);
        hashOps.put(cart.getSkuId().toString(),JSON.toJSONString(cart));
        this.cartAsyncService.updateCartByUserIdAndSkuId(userId.toString(),cart.getSkuId().toString(),cart);
    }

    public void deleteCart(Long skuId) {
        String userId = this.getUserId();
        String key = key_prefix + userId;
        BoundHashOperations<String, Object, Object> hashOps = redisTemplate.boundHashOps(key);
        hashOps.delete(skuId.toString());
        this.cartAsyncService.deleteCart(userId,skuId);
    }

    public List<Cart> queryCheckedCartsByUserId(Long userId) {
        String key = key_prefix+userId;
        BoundHashOperations<String, Object, Object> hashOps = redisTemplate.boundHashOps(key);
        List<Object> cartJsons = hashOps.values();
        if (CollectionUtils.isEmpty(cartJsons)) {
            throw new CartException("该用户没有购物车记录");
        }
        return cartJsons.stream().map(cartJson->JSON.parseObject(cartJson.toString(),Cart.class)).filter(Cart::getCheck).collect(Collectors.toList());

    }
}
