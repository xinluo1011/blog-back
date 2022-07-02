package com.sit.manage.service;

import com.sit.manage.entity.SysComment;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
* @author 星络
* @description 针对表【sys_comment】的数据库操作Service
* @createDate 2022-06-08 17:15:06
*/
public interface SysCommentService extends IService<SysComment> {

    List<SysComment> findCommentDetail(Integer blogId);
}
