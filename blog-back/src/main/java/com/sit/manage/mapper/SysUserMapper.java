package com.sit.manage.mapper;

import com.sit.manage.entity.SysUser;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sit.manage.entity.UserFollow;
import io.swagger.models.auth.In;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Set;

/**
* @author 星络
* @description 针对表【sys_user(用户表)】的数据库操作Mapper
* @createDate 2022-05-27 19:06:33
* @Entity com.sit.manage.entity.SysUser
*/
public interface SysUserMapper extends BaseMapper<SysUser> {

    SysUser checkNickNameSaveInfo(SysUser user);

    SysUser checkSaveInfo(SysUser user);

    @Select("select * from sys_user where id = #{id}")
    SysUser findUserById(@Param("id") Integer id);


    List<UserFollow> selectFollowList(@Param("userId") Integer userId, @Param("page") Integer page, @Param("size") Integer pageSize);

    List<SysUser> findFollowInfoById(Set<Integer> followList);

    Integer findIsFollow(@Param("userId") Integer userId,@Param("followId") Integer followId);

    Integer deleteFollow(@Param("userId") Integer userId,@Param("followId") Integer followId);

    Integer addFollow(UserFollow userFollow);
}




