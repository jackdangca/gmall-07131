package com.atguigu.gmall.cart.listener;

import com.atguigu.gmall.cart.feign.GmallPmsClient;
import com.atguigu.gmall.common.bean.ResponseVo;
import com.atguigu.gmall.pms.entity.SkuEntity;
import com.rabbitmq.client.Channel;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;

@Component
public class CartListener {
    @Autowired
    private GmallPmsClient pmsClient;
    @Autowired
    StringRedisTemplate redisTemplate;
    private static final String price_prefix = "cart:price:";

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = "cart_price_queue",durable = "true"),
            exchange = @Exchange(value = "pms_item_exchange",ignoreDeclarationExceptions = "true",type = ExchangeTypes.TOPIC),
            key = {"item.update"}

    ))
    public void listener(Long spuId, Channel channel, Message message) throws IOException {
        ResponseVo<List<SkuEntity>> listResponseVo = this.pmsClient.querySkusBySpuId(spuId);
        List<SkuEntity> skuEntities = listResponseVo.getData();
        skuEntities.forEach(skuEntity -> {
            if (redisTemplate.hasKey(price_prefix+skuEntity.getId()))
            this.redisTemplate.opsForValue().set(price_prefix+skuEntity.getId(),skuEntity.getPrice().toString());
        });
        channel.basicAck(message.getMessageProperties().getDeliveryTag(),false);
    }
}
