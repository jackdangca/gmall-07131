package com.atguigu.gmall.order.vo;

import com.atguigu.gmall.ums.entity.UserAddressEntity;
import lombok.Data;
import org.apache.catalina.User;

import java.util.List;

@Data
public class OrderConfirmVo {
    private List<UserAddressEntity> addresses;
    private List<OrderItemVo>items;
    private Integer bounds;
    private String orderToken;


}
