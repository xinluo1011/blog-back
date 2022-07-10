package com.sit.manage.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.sit.manage.entity.*;
import com.baomidou.mybatisplus.extension.service.IService;
import com.sit.manage.vo.ResultVO;
import org.springframework.web.bind.annotation.RequestParam;

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

    ResultVO starBlog(BlogStar blogStar);

    ResultVO DeleteStarBlog(Integer blogId,Integer userId);

    ResultVO saveBlog(SysBlog blog);

    ResultVO removeBlog(Integer id);

    ResultVO removeBlogByIds(List<Integer> ids);

    ResultVO findByPage(Integer pageNum, Integer pageSize, String name);

    ResultVO findAll();

    ResultVO findById(Integer id);

    ResultVO searchBlogES(RequestParams params);

    void saveBlogById(Integer id);

    void deleteById(Integer id);
}
