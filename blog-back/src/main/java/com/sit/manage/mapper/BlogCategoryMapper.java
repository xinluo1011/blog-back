package com.sit.manage.mapper;

import com.sit.manage.entity.BlogCategory;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sit.manage.entity.SysCategory;

import java.util.List;

/**
* @author 星络
* @description 针对表【blog_category(博客分类)】的数据库操作Mapper
* @createDate 2022-06-29 22:14:42
* @Entity com.sit.manage.entity.BlogCategory
*/
public interface BlogCategoryMapper extends BaseMapper<BlogCategory> {

    List<String> getCategoryByBlogId(Integer bId);
}




