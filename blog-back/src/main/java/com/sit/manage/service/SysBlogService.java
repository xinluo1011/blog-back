package com.sit.manage.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.sit.manage.entity.SysBlog;
import com.baomidou.mybatisplus.extension.service.IService;
import com.sit.manage.entity.SysMenu;
import com.sit.manage.entity.SysUser;
import com.sit.manage.vo.ResultVO;

import java.util.List;

/**
* @author 星络
* @description 针对表【sys_blog】的数据库操作Service
* @createDate 2022-06-08 11:28:57
*/
public interface SysBlogService extends IService<SysBlog> {

    IPage<SysBlog> findPage(Integer pageNum, Integer pageSize, String name);

    List<SysBlog> findBlogs();

    IPage<SysBlog> findPageByUid(Integer pageNum, Integer pageSize, String name);

    ResultVO like(Integer blogId);
}
