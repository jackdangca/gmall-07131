package com.atguigu.gmall.item.vo;

import com.atguigu.gmall.pms.Vo.ItemGroupVo;
import com.atguigu.gmall.pms.Vo.SaleAttrValueVo;
import com.atguigu.gmall.pms.entity.CategoryEntity;
import com.atguigu.gmall.pms.entity.SkuImagesEntity;
import com.atguigu.gmall.sms.vo.ItemSaleVo;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Data
public class ItemVo {
    private List<CategoryEntity>categories;
    private Long brandId;
    private String brandName;
    private Long spuId;
    private String spuName;
    //sku相关信息
    private String title;
    private String subTitle;
    private BigDecimal price;
    private String defaultImage;
    private Integer weight;
    private Long skuId;
    private List<SkuImagesEntity>skuImages;
    private  List<ItemSaleVo>sales;
    private Boolean store = false;
//    [{attrid:4,attrName:颜色,attrvalues:['暗夜黑','白天白']},{},{}]
    private List<SaleAttrValueVo>saleAttrs;
    //{4:'暗夜黑',5:'8G',6:'128G'}当前选中的值
    private Map<Long,String>saleAttr;
    //销售属性组合和skuid的映射关系
    //{'白色,7g,128g':100,..}
    private String skusJson;


    private List<String> spuImages;
    private List<ItemGroupVo> groups;









}