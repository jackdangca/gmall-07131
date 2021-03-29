package com.atguigu.gmall.pms.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.gmall.common.bean.PageResultVo;
import com.atguigu.gmall.common.bean.PageParamVo;

import com.atguigu.gmall.pms.mapper.CategoryMapper;
import com.atguigu.gmall.pms.entity.CategoryEntity;
import com.atguigu.gmall.pms.service.CategoryService;


@Service("categoryService")
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper, CategoryEntity> implements CategoryService {
    @Autowired
    CategoryMapper categoryMapper;
    @Override
    public PageResultVo queryPage(PageParamVo paramVo) {
        IPage<CategoryEntity> page = this.page(
                paramVo.getPage(),
                new QueryWrapper<CategoryEntity>()
        );

        return new PageResultVo(page);
    }

    @Override
    public List<CategoryEntity> queryCategoriesByPid(Long pid) {
        QueryWrapper<CategoryEntity> wrapper = new QueryWrapper<CategoryEntity>();
        if(pid!=-1){
            wrapper.eq("parent_id",pid);
        }
        return this.list(wrapper);
    }

    @Override
    public List<CategoryEntity> queryCategoriesWithSubsByPid(Long pid) {
        return this.categoryMapper.queryCategoriesWithSubsByPid(pid);
    }

    @Override
    public List<CategoryEntity> queryLv123CategoriesByCid(Long id) {
        CategoryEntity lv3Category = this.getById(id);
        if (lv3Category==null){
            return null;

        }
        CategoryEntity lv2Category = this.getById(lv3Category.getParentId());
        CategoryEntity lv1Category = this.getById(lv2Category.getParentId());

        return Arrays.asList(lv1Category,lv2Category,lv3Category);
    }

}