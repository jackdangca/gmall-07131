package com.atguigu.gmall.order.vo;

import com.atguigu.gmall.ums.entity.UserAddressEntity;
import lombok.Data;

import java.util.List;

@Data
public class OrderSubmitVo {
    private String orderToken;
    private String totalPrice;
    //送货清单:眼总价 盐库存 订单详情
    private List<OrderItemVo>items;
    private Integer payType;
    private String deliveryCompany;
    private UserAddressEntity address;
    private Integer bounds;

}
