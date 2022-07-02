package com.sit.manage.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sit.manage.entity.SysCategory;
import com.sit.manage.entity.SysUser;
import com.sit.manage.service.SysCategoryService;
import com.sit.manage.mapper.SysCategoryMapper;
import org.springframework.stereotype.Service;

/**
* @author 星络
* @description 针对表【sys_category】的数据库操作Service实现
* @createDate 2022-06-09 14:16:04
*/
@Service
public class SysCategoryServiceImpl extends ServiceImpl<SysCategoryMapper, SysCategory>
    implements SysCategoryService{

    @Override
    public IPage<SysCategory> findPage(Integer pageNum, Integer pageSize, String name) {
        IPage<SysCategory> page = new Page<>(pageNum,pageSize);
        QueryWrapper<SysCategory> wrapper = new QueryWrapper<>();
        wrapper.orderByDesc("id");
        if(!"".equals(name)) wrapper.like("name",name);
        return page(page, wrapper);
    }
}




