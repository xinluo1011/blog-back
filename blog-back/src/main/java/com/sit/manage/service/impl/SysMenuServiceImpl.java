package com.sit.manage.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import com.sit.manage.entity.SysMenu;
import com.sit.manage.entity.SysRole;
import com.sit.manage.service.SysMenuService;
import com.sit.manage.mapper.SysMenuMapper;
import com.sit.manage.vo.ResStatus;
import com.sit.manage.vo.ResultVO;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

/**
* @author 星络
* @description 针对表【sys_menu】的数据库操作Service实现
* @createDate 2022-06-06 16:05:45
*/
@Service
public class SysMenuServiceImpl extends ServiceImpl<SysMenuMapper, SysMenu>
    implements SysMenuService{

    @Override
    public IPage<SysMenu> findPage(Integer pageNum, Integer pageSize, String name) {
        IPage<SysMenu> page = new Page<>(pageNum,pageSize);
        QueryWrapper<SysMenu> wrapper = new QueryWrapper<>();
        wrapper.orderByDesc("id");
        if(!"".equals(name))wrapper.like("name",name);
        return page(page,wrapper);
    }

    @Override
    public ResultVO findMenus(String name) {
        QueryWrapper<SysMenu> wrapper = new QueryWrapper<>();
        if(!"".equals(name))wrapper.like("name",name);
        //查询所有数据
        List<SysMenu> list = list();
        //找出pid为null的一级菜单
        List<SysMenu> parentNode = list.stream().filter(sysMenu -> sysMenu.getPid() == null).collect(Collectors.toList());
        //找出一级菜单的子菜单
        for (SysMenu menu : parentNode) {
            //筛选所有数据中pid=父级id的数据就是二级菜单
            List<SysMenu> collect = list.stream().filter(m -> menu.getId().equals(m.getPid())).collect(Collectors.toList());
            menu.setChildren(collect);
        }
        return new ResultVO(ResStatus.SUCCESS,"成功",parentNode);
    }
}




