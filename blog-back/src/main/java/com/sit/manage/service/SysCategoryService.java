package com.sit.manage.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.sit.manage.entity.SysCategory;
import com.baomidou.mybatisplus.extension.service.IService;

/**
* @author 星络
* @description 针对表【sys_category】的数据库操作Service
* @createDate 2022-06-09 14:16:04
*/
public interface SysCategoryService extends IService<SysCategory> {

    IPage<SysCategory> findPage(Integer pageNum, Integer pageSize, String name);
}
