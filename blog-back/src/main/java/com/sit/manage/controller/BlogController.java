package com.sit.manage.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.sit.manage.entity.BlogStar;
import com.sit.manage.entity.RequestParams;
import com.sit.manage.entity.SysBlog;
import com.sit.manage.service.SysBlogService;
import com.sit.manage.util.TokenUtils;
import com.sit.manage.vo.ResStatus;
import com.sit.manage.vo.ResultVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.*;
import javax.annotation.Resource;
import java.util.List;

/**
 * @author 星络
 * @version 1.0
 */
@RestController
@RequestMapping("/blog")
@CrossOrigin
@Api(value = "博客管理", tags="博客管理")
public class BlogController {

    @Resource
    SysBlogService blogService;

    @Resource
    private StringRedisTemplate stringRedisTemplate;


    @ApiOperation("自身博客分页查询")
    @GetMapping("/page/uid")
    public ResultVO findPageByUid(@RequestParam Integer pageNum,
                             @RequestParam Integer pageSize,
                             @RequestParam(defaultValue = "") String name){
        IPage<SysBlog> page = blogService.findPageByUid(pageNum, pageSize, name);
        return new ResultVO(ResStatus.SUCCESS,"成功",page);
    }

    @ApiOperation("新增或修改博客信息")
    @PostMapping("/save")
    public ResultVO save(@RequestBody SysBlog blog){
        return blogService.saveBlog(blog);
    }

    @ApiOperation("删除博客")
    @DeleteMapping("/remove")
    public ResultVO remove(@RequestParam Integer id){
        return blogService.removeBlog(id);
    }

    @ApiOperation("批量删除博客")
    @PostMapping("/removeIds")
    public ResultVO removeByIds(@RequestBody List<Integer> ids){
        return blogService.removeBlogByIds(ids);
    }

    @ApiOperation("博客分页查询")
    @GetMapping("/page")
    public ResultVO findPage(@RequestParam Integer pageNum,
                             @RequestParam Integer pageSize,
                             @RequestParam(defaultValue = "") String name){
        return blogService.findByPage(pageNum,pageSize,name);
    }

    @ApiOperation("全部博客")
    @GetMapping("/all")
    public ResultVO findAll(){
        return blogService.findAll();
    }

    @ApiOperation("通过id找博客")
    @GetMapping("/id/{id}")
    public ResultVO findById(@PathVariable Integer id){
        return blogService.findById(id);
    }

    @ApiOperation("找推荐博客")
    @GetMapping("/is_recommend")
    public ResultVO findByRecommend(){
        QueryWrapper<SysBlog> queryWrapper = new QueryWrapper<>();
        queryWrapper.orderByDesc("id");
        queryWrapper.eq("is_recommend",true);
        List<SysBlog> blogs = blogService.list(queryWrapper);
//        getCName(blogs);
        return new ResultVO(ResStatus.SUCCESS,"成功",blogs);
    }

    @ApiOperation("模糊查询找博客")
    @GetMapping("/name")
    public ResultVO findBlog(@RequestParam String name){
        QueryWrapper<SysBlog> wrapper = new QueryWrapper<>();
        wrapper.orderByDesc("id");
        if(!"".equals(name))wrapper.like("name",name);
        List<SysBlog> blogs = blogService.list(wrapper);
//        getCName(blogs);
        return new ResultVO(ResStatus.SUCCESS,"成功",blogs);
    }

    @ApiOperation("通过分类找博客")
    @GetMapping("/cid/{cid}")
    public ResultVO findByCId(@PathVariable Integer cid){
        QueryWrapper<SysBlog> queryWrapper = new QueryWrapper<>();
        queryWrapper.orderByDesc("id");
        queryWrapper.eq("cid",cid);
        List<SysBlog> blogs = blogService.list(queryWrapper);
//        getCName(blogs);
        return new ResultVO(ResStatus.SUCCESS,"成功",blogs);
    }

//    private void getCName(List<SysBlog> blogs){
//        for (SysBlog blog : blogs) {
//            QueryWrapper<SysCategory> wrapper = new QueryWrapper<>();
//            wrapper.eq("id",blog.getCid());
//            SysCategory category = categoryService.getOne(wrapper);
//            if(category != null){
//                blog.setCName(category.getName());
//            }
//        }
//    }

    @ApiOperation("点赞功能")
    @PostMapping("/like")
    public ResultVO like(@RequestBody SysBlog blog){
        return blogService.like(blog.getId());
    }

    @ApiOperation("收藏功能")
    @PostMapping("/star")
    public ResultVO starBlog(@RequestBody BlogStar blogStar){
        return blogService.starBlog(blogStar);
    }

    @ApiOperation("取消收藏")
    @DeleteMapping("/star")
    public ResultVO deleteStarBlog(@RequestParam Integer blogId){
        Integer userId = TokenUtils.getCurrentUser().getId();
        return blogService.DeleteStarBlog(blogId,userId);
    }

    @ApiOperation("全文搜索")
    @PostMapping("/search")
    public ResultVO searchBlogES(@RequestBody RequestParams params){
        return blogService.searchBlogES(params);
    }

}
