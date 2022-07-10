package com.sit.manage.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sit.manage.entity.BlogStar;
import com.sit.manage.service.BlogStarService;
import com.sit.manage.mapper.BlogStarMapper;
import org.springframework.stereotype.Service;

/**
* @author 星络
* @description 针对表【blog_star(博客收藏)】的数据库操作Service实现
* @createDate 2022-07-02 21:53:33
*/
@Service
public class BlogStarServiceImpl extends ServiceImpl<BlogStarMapper, BlogStar>
    implements BlogStarService{

}




