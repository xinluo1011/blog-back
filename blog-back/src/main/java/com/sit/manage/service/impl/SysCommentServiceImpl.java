package com.sit.manage.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sit.manage.entity.SysComment;
import com.sit.manage.mapper.SysCommentMapper;
import com.sit.manage.service.SysCommentService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
* @author 星络
* @description 针对表【sys_comment】的数据库操作Service实现
* @createDate 2022-06-08 17:15:06
*/
@Service
public class SysCommentServiceImpl extends ServiceImpl<SysCommentMapper, SysComment>
    implements SysCommentService{


    @Resource
    SysCommentMapper commentMapper;

    @Override
    public List<SysComment> findCommentDetail(Integer blogId) {
        return commentMapper.findCommentDetail(blogId);
    }
}




