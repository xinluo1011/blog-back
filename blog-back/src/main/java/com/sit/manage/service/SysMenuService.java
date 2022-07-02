package com.sit.manage.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.sit.manage.entity.SysMenu;
import com.baomidou.mybatisplus.extension.service.IService;
import com.sit.manage.vo.ResultVO;

import java.util.List;

/**
* @author 星络
* @description 针对表【sys_menu】的数据库操作Service
* @createDate 2022-06-06 16:05:45
*/
public interface SysMenuService extends IService<SysMenu> {

    IPage<SysMenu> findPage(Integer pageNum, Integer pageSize, String name);

    ResultVO findMenus(String name);
}
