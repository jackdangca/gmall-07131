package com.atguigu.gmall.pms.service.impl;

import com.atguigu.gmall.pms.entity.*;
import com.atguigu.gmall.pms.feign.GmallPmsClient;
import com.atguigu.gmall.pms.mapper.SkuMapper;
import com.atguigu.gmall.pms.mapper.SpuDescMapper;
import com.atguigu.gmall.pms.service.*;
import com.atguigu.gmall.pms.vo.SkuVo;
import com.atguigu.gmall.pms.vo.SpuAttrValueVo;
import com.atguigu.gmall.pms.vo.SpuVo;
import com.atguigu.gmall.sms.vo.SkuSaleVo;
import io.seata.spring.annotation.GlobalTransactional;
import org.apache.commons.lang.StringUtils;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.FileNotFoundException;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.gmall.common.bean.PageResultVo;
import com.atguigu.gmall.common.bean.PageParamVo;

import com.atguigu.gmall.pms.mapper.SpuMapper;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;


@Service("spuService")
public class SpuServiceImpl extends ServiceImpl<SpuMapper, SpuEntity> implements SpuService {
    @Resource
    SpuDescMapper spuDescMapper;
    @Autowired
    SpuAttrValueService spuAttrValueService;
    @Autowired
    private GmallPmsClient pmsClient;
    @Resource
    private SkuMapper skuMapper;
    @Autowired
    private SkuImagesService imagesService;
    @Autowired
    SkuAttrValueService skuAttrValueService;
    @Autowired
    SpuDescService spuDescService;
    @Autowired
    RabbitTemplate rabbitTemplate;

    @Override
    public PageResultVo queryPage(PageParamVo paramVo) {
        IPage<SpuEntity> page = this.page(
                paramVo.getPage(),
                new QueryWrapper<SpuEntity>()
        );

        return new PageResultVo(page);
    }

    @Override
    public PageResultVo querySpuByCidAndPage(Long cid, PageParamVo paramVo) {
        QueryWrapper<SpuEntity> wrapper = new QueryWrapper<>();
        //如果用户选择了分类,查询本类
        if (cid!=0){
            wrapper.eq("category_id",cid);
        }
        String key = paramVo.getKey();
        if (StringUtils.isNotBlank(key)){
//            wrapper.eq("id",key).or().like("name",key);
            wrapper.and(t->t.eq("id",key).or().like("name",key));
        }
        IPage<SpuEntity> page = this.page(
                paramVo.getPage(),
                wrapper
        );

        return new PageResultVo(page);
    }

    @Override
    @GlobalTransactional
    public void bigSave(SpuVo spu) {
        Long spuId = saveSpu(spu);


        spuDescService.saveSpuDesc(spu);



        //为甚么不是集合就直接能存,这个就不能直接存呢
        saveBaseAttr(spu, spuId);


        saveSku(spu, spuId);
//        int i = 1/0;
        this.rabbitTemplate.convertAndSend("pms_item_exchange","item.insert",spuId);

    }

    private void saveSku(SpuVo spu, Long spuId) {
        List<SkuVo> skus = spu.getSkus();
        if (CollectionUtils.isEmpty(skus)){
            return;

        }
        skus.forEach(sku -> {
            sku.setSpuId(spuId);
            sku.setBrandId(spu.getBrandId());
            sku.setCatagoryId(spu.getCategoryId());
            List<String> images = sku.getImages();
            if (!CollectionUtils.isEmpty(images)){
                sku.setDefaultImage(StringUtils.isNotBlank(sku.getDefaultImage())?sku.getDefaultImage():images.get(0));
            }

            this.skuMapper.insert(sku);
            Long skuId = sku.getId();
            if (!CollectionUtils.isEmpty(images)){


                this.imagesService.saveBatch(images.stream().map(image->{
                    SkuImagesEntity skuImagesEntity = new SkuImagesEntity();
                    skuImagesEntity.setSkuId(sku.getId());
                    skuImagesEntity.setUrl(image);
                    skuImagesEntity.setDefaultStatus(StringUtils.equals(sku.getDefaultImage(),image)?1:0);
                    return skuImagesEntity;
                }).collect(Collectors.toList()));

            }


            List<SkuAttrValueEntity> saleAttrs = sku.getSaleAttrs();
            if (!CollectionUtils.isEmpty(saleAttrs)){
                saleAttrs.forEach(skuAttrValueEntity -> skuAttrValueEntity.setSkuId(skuId));
                this.skuAttrValueService.saveBatch(saleAttrs);
            }


            SkuSaleVo skuSaleVo = new SkuSaleVo();
            BeanUtils.copyProperties(sku,skuSaleVo);
            skuSaleVo.setSkuId(skuId);
            this.pmsClient.saveSales(skuSaleVo);
        });
    }

    private void saveBaseAttr(SpuVo spu, Long spuId) {
        List<SpuAttrValueVo> baseAttrs = spu.getBaseAttrs();
        if (!CollectionUtils.isEmpty(baseAttrs)){
            this.spuAttrValueService.saveBatch(baseAttrs.stream().map(spuAttrValueVo -> {
                SpuAttrValueEntity spuAttrValueEntity = new SpuAttrValueEntity();
                BeanUtils.copyProperties(spuAttrValueVo,spuAttrValueEntity);
                spuAttrValueEntity.setSpuId(spuId);
                return spuAttrValueEntity;
            }).collect(Collectors.toList()));
        }
    }


    private Long saveSpu(SpuVo spu) {
        spu.setCreateTime(new Date());
        spu.setUpdateTime(spu.getCreateTime());
        this.save(spu);
        return spu.getId();
    }

}