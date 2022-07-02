package com.sit.manage.service.impl;

import cn.hutool.core.util.BooleanUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sit.manage.entity.*;
import com.sit.manage.exception.ServiceException;
import com.sit.manage.mapper.BlogCategoryMapper;
import com.sit.manage.mapper.BlogLikeMapper;
import com.sit.manage.mapper.SysUserMapper;
import com.sit.manage.service.SysBlogService;
import com.sit.manage.mapper.SysBlogMapper;
import com.sit.manage.util.TokenUtils;
import com.sit.manage.vo.ResultVO;
import io.swagger.models.auth.In;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.sit.manage.vo.ResStatus.BLOG_KEY;
import static com.sit.manage.vo.ResStatus.BLOG_LIKED;

/**
* @author 星络
* @description 针对表【sys_blog】的数据库操作Service实现
* @createDate 2022-06-08 11:28:57
*/
@Service
public class SysBlogServiceImpl extends ServiceImpl<SysBlogMapper, SysBlog>
    implements SysBlogService{

    @Resource
    SysBlogMapper blogMapper;

    @Resource
    BlogLikeMapper blogLikeMapper;

    @Resource
    BlogCategoryMapper blogCategoryMapper;

    @Resource
    StringRedisTemplate stringRedisTemplate;

    @Override
    public IPage<SysBlog> findPageByUid(Integer pageNum, Integer pageSize, String name) {
        IPage<SysBlog> page = new Page<>(pageNum,pageSize);
        QueryWrapper<SysBlog> wrapper = new QueryWrapper<>();
        wrapper.orderByDesc("id");
        if(!"".equals(name))wrapper.like("name",name);
        wrapper.eq("uid", TokenUtils.getCurrentUser().getId());
        return page(page,wrapper);
    }

    @Override
    public ResultVO like(Integer blogId) {
        //1.获取当前用户
        Integer userId = TokenUtils.getCurrentUser().getId();
        //2.判断当前用户是否点赞
        String key = BLOG_LIKED + userId;
        boolean like = false;
        Double score = stringRedisTemplate.opsForZSet().score(key,userId.toString());
        if(score == null){
            //3.没点赞
            //3.1数据库添加信息
            Integer isSuccess = blogLikeMapper.insertLiked(userId,blogId);
            if(isSuccess != 0){
                stringRedisTemplate.opsForZSet().add(key,userId.toString(),System.currentTimeMillis());
                like = false;
            }
        }else {
            //4.点赞，删除点赞信息
            //4.1删除点赞信息
            blogLikeMapper.deleteLiked(userId,blogId);
            stringRedisTemplate.opsForZSet().remove(key,userId.toString());
            like = true;
        }
        stringRedisTemplate.delete(BLOG_KEY);
        return new ResultVO(100,"ok",like);
    }

    @Override
    public IPage<SysBlog> findPage(Integer pageNum, Integer pageSize, String name) {
        IPage<SysBlog> page = new Page<>(pageNum,pageSize);
        QueryWrapper<SysBlog> wrapper = new QueryWrapper<>();
        wrapper.orderByDesc("id");
        if(!"".equals(name))wrapper.like("name",name);
        return page(page,wrapper);
    }

    @Override
    public List<SysBlog> findBlogs() {
        List<SysBlog> blogs = blogMapper.findBlogs();
        for(SysBlog blog : blogs){
            Integer bId = blog.getId();
            //找分类Id
            List<String> categoryList = blogCategoryMapper.getCategoryByBlogId(bId);
            //查找点赞数
            Integer liked = blogLikeMapper.getBlogLike(bId);
            try{
                //查找当前用户是否已经点赞
                //获取当前用户
                Integer userId = TokenUtils.getCurrentUser().getId();
                Integer isLiked = blogLikeMapper.getIsLiked(bId,userId);
                blog.setLiked(liked);
                blog.setIsLiked(BooleanUtil.isTrue(isLiked > 0));
                blog.setCategoryList(categoryList);
            }catch (Exception ignored){}
        }
        return blogs;
    }


}




