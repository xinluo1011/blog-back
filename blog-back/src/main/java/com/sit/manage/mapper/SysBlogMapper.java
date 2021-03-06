package com.sit.manage.mapper;

import com.sit.manage.entity.BlogStar;
import com.sit.manage.entity.StarGroup;
import com.sit.manage.entity.SysBlog;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
* @author 星络
* @description 针对表【sys_blog】的数据库操作Mapper
* @createDate 2022-06-08 11:28:57
* @Entity com.sit.manage.entity.SysBlog
*/
public interface SysBlogMapper extends BaseMapper<SysBlog> {

    @Select("select * from sys_blog order by id DESC")
    List<SysBlog> findBlogs();


    List<String> getCategoryByBlogId(Integer blogId);

    StarGroup getById(Integer groupId);

    SysBlog getBlogById(Integer blogId);

    void deleteStar(@Param("blogId") Integer blogId,@Param("userId") Integer userId);

    void addStar(BlogStar blogStar);

    Integer getBlogStar(Integer bId);

    Integer getIsStar(@Param("blogId") Integer blogId,@Param("userId") Integer userId);
}




