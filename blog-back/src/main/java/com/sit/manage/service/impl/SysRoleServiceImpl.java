package com.sit.manage.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sit.manage.entity.RoleMenu;
import com.sit.manage.entity.SysMenu;
import com.sit.manage.entity.SysRole;
import com.sit.manage.entity.SysUser;
import com.sit.manage.mapper.RoleMenuMapper;
import com.sit.manage.service.SysMenuService;
import com.sit.manage.service.SysRoleService;
import com.sit.manage.mapper.SysRoleMapper;
import com.sit.manage.vo.ResStatus;
import com.sit.manage.vo.ResultVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
* @author 星络
* @description 针对表【sys_role】的数据库操作Service实现
* @createDate 2022-06-06 16:14:24
*/
@Service
public class SysRoleServiceImpl extends ServiceImpl<SysRoleMapper, SysRole>
    implements SysRoleService{

    @Resource
    private RoleMenuMapper roleMenuMapper;

    @Resource
    private SysMenuService menuService;

    @Override
    public IPage<SysRole> findPage(Integer pageNum, Integer pageSize,String name) {
        IPage<SysRole> page = new Page<>(pageNum,pageSize);
        QueryWrapper<SysRole> wrapper = new QueryWrapper<>();
        wrapper.orderByDesc("id");
        if(!"".equals(name))wrapper.like("name",name);
        return page(page,wrapper);
    }

    @Transactional//事务注解，要么全部成功要么全部失败
    @Override
    public ResultVO setRoleMenu(Integer roleId, List<Integer> menuIds) {
        //先删除当前角色id所有的绑定关系
        roleMenuMapper.deleteByRoleId(roleId);

        List<Integer> resList = new ArrayList<>(menuIds);
        //再把前端传过来的菜单id数组绑定到当前角色id上去
        for (Integer menuId : menuIds) {
            Integer pid = roleMenuMapper.selectByMenuId(menuId);
            if(pid != null && !resList.contains(pid)){//二级菜单 并且传过来的menuId数组里面没有它的父级id
                //将补上这个父级id
                RoleMenu roleMenu = new RoleMenu();
                roleMenu.setRoleId(roleId);
                roleMenu.setMenuId(pid);
                resList.add(pid);
                roleMenuMapper.insert(roleMenu);
            }
            RoleMenu roleMenu = new RoleMenu();
            roleMenu.setRoleId(roleId);
            roleMenu.setMenuId(menuId);
            roleMenuMapper.insert(roleMenu);
        }
        return new ResultVO(ResStatus.SUCCESS,"成功",null);
    }

    @Override
    public ResultVO getRoleMenu(Integer roleId) {
        List<Integer> list = roleMenuMapper.selectByRoleId(roleId);
        List<Integer> resList = new ArrayList<>(list);
        for (Integer menuId : list) {
            Integer pid = roleMenuMapper.selectByMenuId(menuId);
            if(pid != null && list.contains(pid)){
                resList.remove(pid);
            }
        }
        return new ResultVO(ResStatus.SUCCESS,"成功",resList);
    }
}




