package com.sit.manage.controller;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.lang.TypeReference;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.sit.manage.entity.SysBlog;
import com.sit.manage.entity.SysCategory;
import com.sit.manage.service.SysBlogService;
import com.sit.manage.service.SysCategoryService;
import com.sit.manage.util.TokenUtils;
import com.sit.manage.vo.ResStatus;
import com.sit.manage.vo.ResultVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.*;
import javax.annotation.Resource;
import java.util.List;
import java.util.concurrent.TimeUnit;
import static com.sit.manage.util.RedisConstants.LOGIN_USER_TTL;
import static com.sit.manage.vo.ResStatus.BLOG_KEY;

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
    SysCategoryService categoryService;

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
        if (blog.getId() == null){
            blog.setTime(DateUtil.now()); //new Date()
            blog.setUser(TokenUtils.getCurrentUser().getNickname());
            blog.setUid(TokenUtils.getCurrentUser().getId());
            blog.setLiked(0);
        }
        if(blog.getName()==null || blog.getDescription()==null ){//|| blog.getCid() == null
            return new ResultVO(ResStatus.ERROR,"请根据要求输入内容",null);
        }
        boolean save = blogService.saveOrUpdate(blog);
        if(save){
            flushRedis(BLOG_KEY);
            return new ResultVO(ResStatus.SUCCESS,"成功", blog);
        }else {
            return new ResultVO(ResStatus.ERROR,"失败",null);
        }
    }

    @ApiOperation("删除博客")
    @DeleteMapping("/remove")
    public ResultVO remove(@RequestParam Integer id){
        boolean remove = blogService.removeById(id);
        if(remove){
            flushRedis(BLOG_KEY);
            return new ResultVO(ResStatus.SUCCESS,"成功", null);
        }else {
            return new ResultVO(ResStatus.ERROR,"失败",null);
        }
    }

    @ApiOperation("批量删除博客")
    @PostMapping("/removeIds")
    public ResultVO removeByIds(@RequestBody List<Integer> ids){
        boolean remove = blogService.removeByIds(ids);
        if(remove){
            flushRedis(BLOG_KEY);
            return new ResultVO(ResStatus.SUCCESS,"成功", null);
        }else {
            return new ResultVO(ResStatus.ERROR,"失败",null);
        }
    }

    @ApiOperation("博客分页查询")
    @GetMapping("/page")
    public ResultVO findPage(@RequestParam Integer pageNum,
                             @RequestParam Integer pageSize,
                             @RequestParam(defaultValue = "") String name){
        IPage<SysBlog> page = blogService.findPage(pageNum, pageSize, name);
        List<SysBlog> records = page.getRecords();
//        getCName(records);
        return new ResultVO(ResStatus.SUCCESS,"成功",page);
    }

    @ApiOperation("全部博客")
    @GetMapping("/all")
    public ResultVO findAll(){
        List<SysBlog> blogs;
        //1.从缓存获取数据
        String str = stringRedisTemplate.opsForValue().get(BLOG_KEY);
        //2.如果缓存为空
        if(StrUtil.isBlank(str)){
            //3.从数据库中取出数据
            blogs = blogService.findBlogs();
//            getCName(blogs);
            //4.再去缓存到redis
            stringRedisTemplate.opsForValue().set(BLOG_KEY, JSONUtil.toJsonStr(blogs),1,TimeUnit.HOURS);
        }else {
            //5.从redis中获取数据
            blogs = JSONUtil.toBean(str, new TypeReference<List<SysBlog>>(){},true);
        }
        //设置有效期
        stringRedisTemplate.expire(BLOG_KEY,LOGIN_USER_TTL, TimeUnit.HOURS);
        return new ResultVO(ResStatus.SUCCESS,"成功",blogs);
    }

    @ApiOperation("通过id找博客")
    @GetMapping("/id/{id}")
    public ResultVO findById(@PathVariable Integer id){
        SysBlog blog = blogService.getById(id);
        return new ResultVO(ResStatus.SUCCESS,"成功",blog);
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


    /**
     * 当进行删除或修改时对缓存进行删除
     * @param key redis中的key
     */
    private void flushRedis(String key){
        stringRedisTemplate.delete(key);
    }
}
