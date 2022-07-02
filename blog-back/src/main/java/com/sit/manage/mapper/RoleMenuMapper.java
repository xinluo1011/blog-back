package com.sit.manage.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sit.manage.entity.RoleMenu;
import com.sit.manage.entity.SysMenu;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * @author 星络
 * @version 1.0
 */
public interface RoleMenuMapper extends BaseMapper<RoleMenu> {

    @Delete("delete from sys_role_menu where role_id = #{roleId}")
    int deleteByRoleId(@Param("roleId") Integer roleId);

    @Select("select menu_id from sys_role_menu where role_id = #{roleId}")
    List<Integer> selectByRoleId(@Param("roleId") Integer roleId);

    @Select("select pid from sys_menu where id = #{menuId}")
    Integer selectByMenuId(@Param("menuId") Integer menuId);
}
