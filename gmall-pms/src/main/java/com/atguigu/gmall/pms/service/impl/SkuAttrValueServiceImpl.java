package com.atguigu.gmall.pms.service.impl;

import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.pms.Vo.SaleAttrValueVo;
import com.atguigu.gmall.pms.entity.AttrEntity;
import com.atguigu.gmall.pms.entity.SkuEntity;
import com.atguigu.gmall.pms.mapper.AttrMapper;
import com.atguigu.gmall.pms.mapper.SkuMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.gmall.common.bean.PageResultVo;
import com.atguigu.gmall.common.bean.PageParamVo;

import com.atguigu.gmall.pms.mapper.SkuAttrValueMapper;
import com.atguigu.gmall.pms.entity.SkuAttrValueEntity;
import com.atguigu.gmall.pms.service.SkuAttrValueService;
import org.springframework.util.CollectionUtils;


@Service("skuAttrValueService")
public class SkuAttrValueServiceImpl extends ServiceImpl<SkuAttrValueMapper, SkuAttrValueEntity> implements SkuAttrValueService {
    @Autowired
    AttrMapper attrMapper;
    @Autowired
    private SkuMapper skuMapper;
    @Autowired
    private SkuAttrValueMapper attrValueMapper;

    @Override
    public PageResultVo queryPage(PageParamVo paramVo) {
        IPage<SkuAttrValueEntity> page = this.page(
                paramVo.getPage(),
                new QueryWrapper<SkuAttrValueEntity>()
        );

        return new PageResultVo(page);
    }

    @Override
    public List<SkuAttrValueEntity> querySearchSkuAttrValuesByCidAndSkuId(Long cid, Long skuId) {
        List<AttrEntity> attrEntities = this.attrMapper.selectList(new QueryWrapper<AttrEntity>().eq("search_type", 1).eq("category_id", cid));
        if (CollectionUtils.isEmpty(attrEntities)){
            return null;
        }
        List<Long> ids = attrEntities.stream().map(AttrEntity::getId).collect(Collectors.toList());
        return this.list(new QueryWrapper<SkuAttrValueEntity>().eq("sku_id",skuId).in("attr_id",ids));

    }

    @Override
    public List<SaleAttrValueVo> querySaleAttrValueBySpuId(Long spuId) {
        List<SkuEntity> skuEntities = this.skuMapper.selectList(new QueryWrapper<SkuEntity>().eq("spu_id", spuId));
        if (CollectionUtils.isEmpty(skuEntities)) {
            return null;
        }
        List<Long> skuIds = skuEntities.stream().map(SkuEntity::getId).collect(Collectors.toList());
        List<SkuAttrValueEntity> skuAttrValues = this.list(new QueryWrapper<SkuAttrValueEntity>().in("sku_id", skuIds));
        if (CollectionUtils.isEmpty(skuAttrValues)) {
            return null;
        }
        //要把list<skuattrvalueentity>>处理成list<saleattrvaluevo>
        //要把list<skuattrvalueentity>处理成map<attrid,list<skuattrvalueentity>>
        Map<Long, List<SkuAttrValueEntity>> map = skuAttrValues.stream().collect(Collectors.groupingBy(SkuAttrValueEntity::getAttrId));
        List<SaleAttrValueVo> attrValueVos = new ArrayList<>();
        map.forEach((attrId,skuAttrValueEntities)->{
            SaleAttrValueVo saleAttrValueVo = new SaleAttrValueVo();
            saleAttrValueVo.setAttrId(attrId);
            saleAttrValueVo.setAttrName(skuAttrValueEntities.get(0).getAttrName());
            saleAttrValueVo.setAttrValues(skuAttrValueEntities.stream().map(SkuAttrValueEntity::getAttrValue).collect(Collectors.toSet()));
            attrValueVos.add(saleAttrValueVo);
        });
        return attrValueVos;
    }

    @Override
    public String querySaleAttrValuesMappingSkuIdBySpuId(Long spuId) {
        List<Map<String, Object>> maps = this.attrValueMapper.querySaleAttrValuesMappingSkuIdBySpuId(spuId);
        if (CollectionUtils.isEmpty(maps)) {
            return null;
        }
        Map<String, Long> attrValuesMappingSkuIdMap = maps.stream().collect(Collectors.toMap(map -> (String)map.get("attr_values"), map ->(Long) map.get("sku_id")));

        return JSON.toJSONString(attrValuesMappingSkuIdMap);
    }

}