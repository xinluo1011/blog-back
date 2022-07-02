package com.sit.manage.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sit.manage.entity.BlogCategory;
import com.sit.manage.service.BlogCategoryService;
import com.sit.manage.mapper.BlogCategoryMapper;
import org.springframework.stereotype.Service;

/**
* @author 星络
* @description 针对表【blog_category(博客分类)】的数据库操作Service实现
* @createDate 2022-06-29 22:14:42
*/
@Service
public class BlogCategoryServiceImpl extends ServiceImpl<BlogCategoryMapper, BlogCategory>
    implements BlogCategoryService{

}




