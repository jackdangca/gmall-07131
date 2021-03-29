package com.atguigu.gmall.wms.vo;

import lombok.Data;

@Data
public class SkuLockVo {
    private Long skuId;
    private Integer count;
    private Boolean lock;
    private Long wareSkuId;//记录锁定成功的库存的id,方便以后解锁库存
}
