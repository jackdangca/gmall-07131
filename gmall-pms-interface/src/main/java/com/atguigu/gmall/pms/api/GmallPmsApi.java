package com.atguigu.gmall.pms.api;

import com.atguigu.gmall.common.bean.PageParamVo;
import com.atguigu.gmall.common.bean.ResponseVo;
import com.atguigu.gmall.pms.Vo.ItemGroupVo;
import com.atguigu.gmall.pms.Vo.SaleAttrValueVo;
import com.atguigu.gmall.pms.entity.*;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import java.util.List;

public interface GmallPmsApi {
    @PostMapping("pms/spu/page")
    @ApiOperation("分页查询")
    public ResponseVo<List<SpuEntity>> querySpuByPageJson(@RequestBody PageParamVo paramVo);
    @GetMapping("pms/sku/spu/{spuId}")
    public ResponseVo<List<SkuEntity>>querySkusBySpuId(@PathVariable("spuId")Long spuId);
    @GetMapping("pms/brand/{id}")
    @ApiOperation("详情查询")
    public ResponseVo<BrandEntity> queryBrandById(@PathVariable("id") Long id);
    @GetMapping("pms/category/{id}")
    @ApiOperation("详情查询")
    public ResponseVo<CategoryEntity> queryCategoryById(@PathVariable("id") Long id);
    @GetMapping("pms/skuattrvalue/category/{cid}")
    public ResponseVo<List<SkuAttrValueEntity>>querySearchSkuAttrValuesByCidAndSkuId(
            @PathVariable("cid") Long cid,
            @RequestParam("sku_id")Long skuId
    );
    @GetMapping("pms/spuattrvalue/category/{cid}")
    public ResponseVo<List<SpuAttrValueEntity>>querySearchSpuAttrValuesByCidAndSpuId(
            @PathVariable("cid") Long cid,
            @RequestParam("spu_id")Long spuId
    );
    @GetMapping("pms/spu/{id}")
    @ApiOperation("详情查询")
    public ResponseVo<SpuEntity> querySpuById(@PathVariable("id") Long id);
    @GetMapping("pms/category/parent/{parentId}")
    public ResponseVo<List<CategoryEntity>>queryCategoriesByPid(@PathVariable("parentId")Long pid);
    @GetMapping("pms/category/parent/withsub/{pid}")
    public ResponseVo<List<CategoryEntity>>queryCategoriesWithSubsByPid(@PathVariable("pid")Long pid);
    @GetMapping("pms/sku/{id}")
    @ApiOperation("详情查询")
    public ResponseVo<SkuEntity> querySkuById(@PathVariable("id") Long id);
    @GetMapping("pms/category/all/{id}")
    public ResponseVo<List<CategoryEntity>> queryLv123CategoriesByCid(@PathVariable("id")Long id);
    @GetMapping("pms/skuimages/sku/{skuId}")
    public ResponseVo<List<SkuImagesEntity>> queryImagesBySkuId(@PathVariable("skuId") Long skuId);
    @GetMapping("pms/skuattrvalue/spu/{spuId}")
    public ResponseVo<List<SaleAttrValueVo>>querySaleAttrValueBySpuId(@PathVariable("spuId")Long spuId);
    @GetMapping("pms/skuattrvalue/sku/{skuId}")
    public ResponseVo<List<SkuAttrValueEntity>>querySaleAttrValueBySkuId(@PathVariable("skuId")Long skuId);
    @GetMapping("pms/spudesc/{spuId}")
    @ApiOperation("详情查询")
    public ResponseVo<SpuDescEntity> querySpuDescById(@PathVariable("spuId") Long spuId);
    @GetMapping("pms/attrgroup/withattr/value/category/{cid}")
    public ResponseVo<List<ItemGroupVo>>queryGroupWithAttrsAndValuesByCidAndSpuIdAndSkuId(
            @PathVariable("cid")Long cid,
            @RequestParam("skuId") Long skuId,
            @RequestParam("spuId")Long spuId
    );
    @GetMapping("pms/skuattrvalue/mapping/spu/{spuId}")
    public ResponseVo<String> querySaleAttrValuesMappingSkuIdBySpuId(@PathVariable("spuId")Long spuId);
}
