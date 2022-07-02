package com.sit.manage.controller;

import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.sit.manage.entity.SysComment;
import com.sit.manage.entity.SysMenu;
import com.sit.manage.service.SysCommentService;
import com.sit.manage.vo.ResStatus;
import com.sit.manage.vo.ResultVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.models.auth.In;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author 星络
 * @version 1.0
 */
@RestController
@RequestMapping("/comment")
@CrossOrigin
@Api(value = "评论管理", tags="评论管理")
public class CommentController {

    @Resource
    SysCommentService commentService;

    @ApiOperation("新增或修改评论信息")
    @PostMapping("/save")
    public ResultVO save(@RequestBody SysComment comment){
        if (comment.getId() == null){
            comment.setTime(DateUtil.now());
            Integer pid = comment.getPid();
            if (comment.getPid()!=null){
                SysComment pComment = commentService.getById(pid);
                if (pComment.getOriginId() != null){ //如果当前回复的父级有祖宗那么就设置相同的祖宗
                    comment.setOriginId(pComment.getOriginId());
                }else {//否则就设置父级为当前的祖宗
                    comment.setOriginId(comment.getPid());
                }
            }

        }
        boolean save = commentService.saveOrUpdate(comment);
        if(save){
            return new ResultVO(ResStatus.SUCCESS,"成功", comment);
        }else {
            return new ResultVO(ResStatus.ERROR,"失败",null);
        }

    }

    @GetMapping("/tree/{blogId}")
    public ResultVO findTree(@PathVariable Integer blogId){
        //查询所有评论和回复数据
        List<SysComment> blogComments = commentService.findCommentDetail(blogId);
        //筛选评论数据（不包括回复）
        List<SysComment> originList = blogComments.stream().filter(comment -> comment.getOriginId() == null).collect(Collectors.toList());
        //设置评论数据的子节点
        for (SysComment origin : originList) {
            //找出回复评论
            List<SysComment> reply = blogComments.stream().filter(comment -> origin.getId().equals(comment.getOriginId())).collect(Collectors.toList());
            reply.forEach(comment -> {
                blogComments.stream().filter(c1->c1.getId().equals(comment.getPid())).findFirst().ifPresent(v->{ //找到父级评论的用户id和用户昵称，并设置给当前的回复评论对象
                    comment.setPUserId(v.getUid());
                    comment.setPNickName(v.getNickname());
                });
            });
            origin.setChildren(reply);
        }

        return new ResultVO(ResStatus.SUCCESS,"成功",originList);

    }

    @ApiOperation("删除评论")
    @DeleteMapping("/remove")
    public ResultVO remove(@RequestParam Integer id){
        boolean remove = commentService.removeById(id);
        if(remove){
            return new ResultVO(ResStatus.SUCCESS,"成功", null);
        }else {
            return new ResultVO(ResStatus.ERROR,"失败",null);
        }
    }

}
