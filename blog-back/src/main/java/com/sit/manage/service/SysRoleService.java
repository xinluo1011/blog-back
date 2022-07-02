package com.sit.manage.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.sit.manage.entity.SysRole;
import com.baomidou.mybatisplus.extension.service.IService;
import com.sit.manage.entity.SysUser;
import com.sit.manage.vo.ResultVO;

import java.util.List;

/**
* @author 星络
* @description 针对表【sys_role】的数据库操作Service
* @createDate 2022-06-06 16:14:25
*/
public interface SysRoleService extends IService<SysRole> {

    IPage<SysRole> findPage(Integer pageNum, Integer pageSize, String name);

    ResultVO setRoleMenu(Integer roleId, List<Integer> menuIds);

    ResultVO getRoleMenu(Integer roleId);
}
