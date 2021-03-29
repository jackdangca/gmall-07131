package com.atguigu.gmall.item.service;

import com.atguigu.gmall.common.bean.ResponseVo;
import com.atguigu.gmall.common.exception.ItemException;
import com.atguigu.gmall.item.feign.GmallPmsClient;
import com.atguigu.gmall.item.feign.GmallSmsClient;
import com.atguigu.gmall.item.feign.GmallWmsClient;
import com.atguigu.gmall.item.vo.ItemVo;
import com.atguigu.gmall.pms.Vo.ItemGroupVo;
import com.atguigu.gmall.pms.Vo.SaleAttrValueVo;
import com.atguigu.gmall.pms.entity.*;
import com.atguigu.gmall.sms.vo.ItemSaleVo;
import com.atguigu.gmall.wms.entity.WareSkuEntity;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import javax.annotation.Resource;
import java.io.File;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.stream.Collectors;

@Service
public class ItemService {
    @Autowired
    private GmallPmsClient pmsClient;
    @Autowired
    private GmallSmsClient smsClient;
    @Autowired
    private GmallWmsClient wmsClient;
    @Autowired
    private ThreadPoolExecutor threadPoolExecutor;
    @Autowired
    private TemplateEngine templateEngine;
    private void createHtml(Long skuId){
        ItemVo itemVo = this.loadData(skuId);
        Context context = new Context();
        context.setVariable("itemVo",itemVo);
        try (PrintWriter printWriter = new PrintWriter(new File("D:\\Users\\IdeaProjects\\html\\" + skuId + ".html"));
        ){
            templateEngine.process("item",context,printWriter);
        }catch (Exception e){
            e.printStackTrace();
        }

    }
    public void asyncExecute(Long skuId){
        threadPoolExecutor.execute(()->createHtml(skuId));
    }


    public ItemVo loadData(Long skuId) {
        ItemVo itemVo = new ItemVo();
        //查询sku信息
        CompletableFuture<SkuEntity> skuFuture = CompletableFuture.supplyAsync(() -> {
            ResponseVo<SkuEntity> skuEntityResponseVo = this.pmsClient.querySkuById(skuId);
            SkuEntity skuEntity = skuEntityResponseVo.getData();
            if (skuEntity == null) {
                throw new ItemException("该skuid对应的商品不存在");

            }
            itemVo.setSkuId(skuEntity.getId());
            itemVo.setTitle(skuEntity.getTitle());
            itemVo.setSubTitle(skuEntity.getSubtitle());
            itemVo.setPrice(skuEntity.getPrice());
            itemVo.setWeight(skuEntity.getWeight());
            itemVo.setDefaultImage(skuEntity.getDefaultImage());
            return skuEntity;

        }, threadPoolExecutor);

        //查询分类信息
        CompletableFuture<Void> future1 = skuFuture.thenAcceptAsync(skuEntity -> {
            ResponseVo<List<CategoryEntity>> categoryResponseVo = this.pmsClient.queryLv123CategoriesByCid(skuEntity.getCatagoryId());
            List<CategoryEntity> categoryEntities = categoryResponseVo.getData();
            itemVo.setCategories(categoryEntities);
        }, threadPoolExecutor);

        //查询品牌信息
        CompletableFuture<Void> future2 = skuFuture.thenAcceptAsync(skuEntity -> {
            ResponseVo<BrandEntity> brandEntityResponseVo = this.pmsClient.queryBrandById(skuEntity.getBrandId());
            BrandEntity brandEntity = brandEntityResponseVo.getData();
            if (brandEntity != null) {
                itemVo.setBrandId(brandEntity.getId());
                itemVo.setBrandName(brandEntity.getName());

            }

        }, threadPoolExecutor);


        //查询spu信息
        CompletableFuture<Void> future3 = skuFuture.thenAcceptAsync(skuEntity -> {
            ResponseVo<SpuEntity> spuEntityResponseVo = this.pmsClient.querySpuById(skuEntity.getId());
            SpuEntity spuEntity = spuEntityResponseVo.getData();
            if (spuEntity != null) {
                itemVo.setSpuId(spuEntity.getId());
                itemVo.setSpuName(spuEntity.getName());
            }
        }, threadPoolExecutor);


        //sku图片列表
        CompletableFuture<Void> future4 = CompletableFuture.runAsync(() -> {
            ResponseVo<List<SkuImagesEntity>> imageResponseVo = this.pmsClient.queryImagesBySkuId(skuId);
            List<SkuImagesEntity> skuImagesEntities = imageResponseVo.getData();
            itemVo.setSkuImages(skuImagesEntities);

        }, threadPoolExecutor);

        //营销信息
        CompletableFuture<Void> future5 = CompletableFuture.runAsync(() -> {
            ResponseVo<List<ItemSaleVo>> salesResponseVo = this.smsClient.querySalesBySkuId(skuId);
            List<ItemSaleVo> itemSaleVos = salesResponseVo.getData();
            itemVo.setSales(itemSaleVos);
        }, threadPoolExecutor);

        //鲁村信息
        CompletableFuture<Void> future6 = CompletableFuture.runAsync(() -> {
            ResponseVo<List<WareSkuEntity>> wareResponseVo = this.wmsClient.queryWareSkusBySkuId(skuId);
            List<WareSkuEntity> wareSkuEntities = wareResponseVo.getData();
            if (!CollectionUtils.isEmpty(wareSkuEntities)) {
                itemVo.setStore(wareSkuEntities.stream().anyMatch(wareSkuEntity -> wareSkuEntity.getStock() - wareSkuEntity.getStockLocked() > 0));


            }
        }, threadPoolExecutor);

        //查询spu销售属性
        CompletableFuture<Void> future7 = skuFuture.thenAcceptAsync(skuEntity -> {
            ResponseVo<List<SaleAttrValueVo>> saleAttrsResponseVo = this.pmsClient.querySaleAttrValueBySpuId(skuEntity.getSpuId());
            List<SaleAttrValueVo> saleAttrValueVos = saleAttrsResponseVo.getData();
            itemVo.setSaleAttrs(saleAttrValueVos);
        }, threadPoolExecutor);


        //查询sku销售属性{4:'暗夜黑',5:'8g'}
        CompletableFuture<Void> future8 = CompletableFuture.runAsync(() -> {
            ResponseVo<List<SkuAttrValueEntity>> skuAttrResponseVo = this.pmsClient.querySaleAttrValueBySkuId(skuId);
            List<SkuAttrValueEntity> skuAttrValueEntities = skuAttrResponseVo.getData();
            if (!CollectionUtils.isEmpty(skuAttrValueEntities)) {
                itemVo.setSaleAttr(skuAttrValueEntities.stream().collect(Collectors.toMap(SkuAttrValueEntity::getAttrId, SkuAttrValueEntity::getAttrValue)));
            }

        }, threadPoolExecutor);

        //映射关系
        CompletableFuture<Void> future9 = skuFuture.thenAcceptAsync(skuEntity -> {
            ResponseVo<String> skuMappingResponseVo = this.pmsClient.querySaleAttrValuesMappingSkuIdBySpuId(skuEntity.getSpuId());
            String data = skuMappingResponseVo.getData();
            itemVo.setSkusJson(data);
        }, threadPoolExecutor);


        //查询商品详情
        CompletableFuture<Void> future10 = skuFuture.thenAcceptAsync(skuEntity -> {
            ResponseVo<SpuDescEntity> spuDescEntityResponseVo = this.pmsClient.querySpuDescById(skuEntity.getSpuId());
            SpuDescEntity spuDescEntity = spuDescEntityResponseVo.getData();
            if (spuDescEntity != null) {
                List<String> strings = Arrays.asList(StringUtils.split(spuDescEntity.getDecript(), ","));
                itemVo.setSpuImages(strings);
            }

        }, threadPoolExecutor);

        //规格参数组
        CompletableFuture<Void> future11 = skuFuture.thenAcceptAsync(skuEntity -> {
            ResponseVo<List<ItemGroupVo>> groupResponseVo = this.pmsClient.queryGroupWithAttrsAndValuesByCidAndSpuIdAndSkuId(skuEntity.getCatagoryId(), skuId, skuEntity.getSpuId());
            List<ItemGroupVo> groupVos = groupResponseVo.getData();

            itemVo.setGroups(groupVos);
        }, threadPoolExecutor);


        CompletableFuture.allOf(future1,future2,future3,future4,future5,future6,future7,future8,future8,future10,future11).join();

        return itemVo;
    }

}
class CompletableFutureDemo{
    public static void main(String[] args) {
        CompletableFuture.runAsync(()->{
            System.out.println("completable初始化任务");
        });
        CompletableFuture<String> future = CompletableFuture.supplyAsync(() -> {
            System.out.println("completable初始化任务");
            return "hello completablefuture";
        });
        future
                .thenApplyAsync(t->{
            System.out.println("=======thenapplyasync======");
            System.out.println("上一个任务的返回结果"+t);
            return "thenapplyasync";
        });
                future.thenAcceptAsync(t->{
            System.out.println("=======thenapplyasync======");
            System.out.println("上一个任务的返回结果"+t);
        });
                future.thenRunAsync(()->{
                System.out.println("=======thenrunasync======");
        });
//                .whenCompleteAsync((t,u)->{
//            System.out.println(t);
//            System.out.println(u);
//        });
        System.out.println("主线程.....");
    }


}