package com.atguigu.gmall.sms.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class SkuSaleVo {
    private Long skuId;
    private BigDecimal growBounds;

    private BigDecimal buyBounds;

    private List<Integer> work;
    private Integer fullCount;

    private BigDecimal discount;

    private Integer ladderAddOther;
    private BigDecimal fullPrice;

    private BigDecimal reducePrice;

    private Integer fullAddOther;
}
