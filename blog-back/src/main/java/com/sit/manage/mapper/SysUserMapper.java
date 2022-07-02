package com.sit.manage.mapper;

import com.sit.manage.entity.SysUser;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

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
}




