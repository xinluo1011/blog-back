package com.sit.manage.mapper;

import com.sit.manage.entity.SysComment;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
* @author 星络
* @description 针对表【sys_comment】的数据库操作Mapper
* @createDate 2022-06-08 17:15:06
* @Entity com.sit.manage.entity.SysComment
*/
public interface SysCommentMapper extends BaseMapper<SysComment> {


    @Select("select c.*,u.nickname,u.avatar_url from sys_comment c left join sys_user u on c.uid = u.id "+
            "where c.blog_id = #{blogId} order by id desc")
    List<SysComment> findCommentDetail(@Param("blogId") Integer blogId);
}




