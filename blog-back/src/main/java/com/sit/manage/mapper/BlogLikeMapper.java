package com.sit.manage.mapper;

import com.sit.manage.entity.BlogLike;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

/**
* @author 星络
* @description 针对表【blog_like(博客点赞)】的数据库操作Mapper
* @createDate 2022-06-29 21:00:19
* @Entity com.sit.manage.entity.BlogLike
*/
public interface BlogLikeMapper extends BaseMapper<BlogLike> {

    //查找点赞数
    Integer getBlogLike(Integer bId);

    Integer getIsLiked(@Param("blogId") Integer bId,@Param("userId") Integer userId);

    Integer insertLiked(@Param("userId") Integer userId,@Param("blogId") Integer blogId);

    Integer deleteLiked(@Param("userId") Integer userId,@Param("blogId") Integer blogId);
}




