package com.sit.manage.service.impl;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.lang.TypeReference;
import cn.hutool.core.util.BooleanUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sit.manage.entity.*;
import com.sit.manage.exception.ServiceException;
import com.sit.manage.mapper.*;
import com.sit.manage.service.SysBlogService;
import com.sit.manage.util.MqConstants;
import com.sit.manage.util.TokenUtils;
import com.sit.manage.vo.ResStatus;
import com.sit.manage.vo.ResultVO;
import io.swagger.models.auth.In;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.functionscore.FunctionScoreQueryBuilder;
import org.elasticsearch.index.query.functionscore.ScoreFunctionBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.sit.manage.util.RedisConstants.LOGIN_USER_TTL;
import static com.sit.manage.vo.ResStatus.*;

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

    @Resource
    RestHighLevelClient client;

    @Resource
    RabbitTemplate rabbitTemplate;

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
        String key = BLOG_LIKED + blogId;
        boolean like = false;
        Double score = stringRedisTemplate.opsForZSet().score(key,userId.toString());
        if(score == null){
            //3.没点赞
            //3.1数据库添加信息
            Integer isSuccess = blogLikeMapper.insertLiked(userId,blogId);
            if(isSuccess != 0){
                stringRedisTemplate.opsForZSet().add(key,userId.toString(),System.currentTimeMillis());
                stringRedisTemplate.opsForHash().increment(BLOG_LIKED_KEY,blogId.toString(),1);
                like = false;
            }
        }else {
            //4.点赞，删除点赞信息
            //4.1删除点赞信息
            blogLikeMapper.deleteLiked(userId,blogId);
            stringRedisTemplate.opsForZSet().remove(key,userId.toString());
            stringRedisTemplate.opsForHash().increment(BLOG_LIKED_KEY,blogId.toString(),-1);
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
            //查找收藏数
            Integer star = blogMapper.getBlogStar(bId);
            try{
                //查找当前用户是否已经点赞
                //获取当前用户
                Integer userId = TokenUtils.getCurrentUser().getId();
                Integer isLiked = blogLikeMapper.getIsLiked(bId,userId);
                //查找当前用户是否已经收藏
                Integer isStar = blogMapper.getIsStar(bId,userId);
                blog.setLiked(liked);
                blog.setStared(star);
                blog.setIsLiked(BooleanUtil.isTrue(isLiked > 0));
                blog.setIsStared(BooleanUtil.isTrue(isStar > 0));
                blog.setCategoryList(categoryList);
                stringRedisTemplate.opsForHash().put(BLOG_LIKED_KEY,bId.toString(),liked.toString());
                stringRedisTemplate.opsForHash().put(BLOG_STAR_KEY,bId.toString(),star.toString());
            }catch (Exception ignored){}
        }
        return blogs;
    }

    /**
     * 博客收藏
     * @param blogStar 博客收藏信息
     * @return ResultVO成功结果
     */
    @Override
    @Transactional
    public ResultVO starBlog(BlogStar blogStar) {
        //1.获取当前用户Id
        Integer userId = TokenUtils.getCurrentUser().getId();
        //2.获取博客Id和分组Id
        Integer blogId = blogStar.getBlogId();
        Integer groupId = blogStar.getGroupId();
        //3.判断分组是否为空，如果为空就将收藏放入默认收藏夹
        if(groupId == null){
            blogStar.setGroupId(1);
        }else {
            //4.如果不为空，判断分组Id是否存在
            StarGroup starGroup = blogMapper.getById(groupId);
            if(starGroup == null){
                //4.1不存在抛出异常
                throw new ServiceException("分组不存在！");
            }
        }
        //5.判断博客是否存在
        if(blogId == null){
            //5.1判断博客Id是否合法
            throw new ServiceException("参数异常！");
        }
        //5.2查找博客判断博客是否存在
        SysBlog blog = blogMapper.getBlogById(blogId);
        if(blog == null){
            throw new ServiceException("非法博客！");
        }
        //6.删除原有的博客收藏信息
        blogMapper.deleteStar(blogId,userId);
        //7.添加新的博客收藏信息
        blogStar.setUserId(userId);
        blogMapper.addStar(blogStar);
        stringRedisTemplate.delete(BLOG_KEY);
        stringRedisTemplate.opsForSet().add(BLOG_STAR+blogId,userId.toString());
        stringRedisTemplate.opsForHash().increment(BLOG_STAR_KEY,blogId.toString(),1);
        return new ResultVO(100,"成功",null);
    }

    /**
     * 取消收藏
     * @param blogId 博客Id
     * @param userId 用户Id
     * @return ResultVO 成功结果
     */
    @Override
    public ResultVO DeleteStarBlog(Integer blogId,Integer userId) {
        blogMapper.deleteStar(blogId,userId);
        stringRedisTemplate.opsForSet().remove(BLOG_STAR+blogId,userId.toString());
        stringRedisTemplate.opsForHash().increment(BLOG_STAR_KEY,blogId.toString(),-1);
        return new ResultVO(100,"成功",null);
    }

    @Override
    public ResultVO saveBlog(SysBlog blog) {
        if (blog.getId() == null){
            blog.setTime(DateUtil.now()); //new Date()
            blog.setUser(TokenUtils.getCurrentUser().getNickname());
            blog.setUid(TokenUtils.getCurrentUser().getId());
            blog.setLiked(0);
        }
        if(blog.getName()==null || blog.getDescription()==null ){//|| blog.getCid() == null
            return new ResultVO(ResStatus.ERROR,"请根据要求输入内容",null);
        }
        boolean save = saveOrUpdate(blog);
        if(save){
            flushRedis(BLOG_KEY);
            rabbitTemplate.convertAndSend(MqConstants.BLOG_EXCHANGE,MqConstants.BLOG_INSERT_KEY,blog.getId());
            return new ResultVO(ResStatus.SUCCESS,"成功", blog);
        }else {
            return new ResultVO(ResStatus.ERROR,"失败",null);
        }
    }

    @Override
    public ResultVO removeBlog(Integer id) {
        boolean remove = removeById(id);
        if(remove){
            flushRedis(BLOG_KEY);
            rabbitTemplate.convertAndSend(MqConstants.BLOG_EXCHANGE,MqConstants.BLOG_DELETE_KEY,id);
            return new ResultVO(ResStatus.SUCCESS,"成功", null);
        }else {
            return new ResultVO(ResStatus.ERROR,"失败",null);
        }
    }

    @Override
    public ResultVO removeBlogByIds(List<Integer> ids) {
        boolean remove = removeByIds(ids);
        if(remove){
            flushRedis(BLOG_KEY);
            return new ResultVO(ResStatus.SUCCESS,"成功", null);
        }else {
            return new ResultVO(ResStatus.ERROR,"失败",null);
        }
    }

    @Override
    public ResultVO findByPage(Integer pageNum, Integer pageSize, String name) {
        IPage<SysBlog> page = findPage(pageNum, pageSize, name);
        List<SysBlog> records = page.getRecords();
//        getCName(records);
        return new ResultVO(ResStatus.SUCCESS,"成功",page);
    }

    @Override
    public ResultVO findAll() {
        List<SysBlog> blogs;
        //1.从缓存获取数据
        String str = stringRedisTemplate.opsForValue().get(BLOG_KEY);
        //2.如果缓存为空
        if(StrUtil.isBlank(str)){
            //3.从数据库中取出数据
            blogs = findBlogs();
            for(SysBlog blog : blogs){
                List<String> categoryByBlogId = blogMapper.getCategoryByBlogId(blog.getId());
                blog.setCategoryList(categoryByBlogId);
            }
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

    @Override
    public ResultVO findById(Integer id) {
        Integer userId = TokenUtils.getCurrentUser().getId();
        SysBlog blog = getById(id);
        Object like = stringRedisTemplate.opsForHash().get(BLOG_LIKED_KEY, id.toString());
        Object star = stringRedisTemplate.opsForHash().get(BLOG_STAR_KEY, id.toString());
        Double score = stringRedisTemplate.opsForZSet().score(BLOG_LIKED + id, userId.toString());
        Boolean member = stringRedisTemplate.opsForSet().isMember(BLOG_STAR + id, userId.toString());
        blog.setIsLiked(BooleanUtil.isTrue(score != null));
        blog.setIsStared(BooleanUtil.isTrue(member));
        blog.setStared(Integer.valueOf(star.toString()));
        blog.setLiked(Integer.valueOf(like.toString()));
        return new ResultVO(ResStatus.SUCCESS,"成功",blog);
    }

    @Override
    public ResultVO searchBlogES(RequestParams params) {
        try {
            PageResult pageResult = new PageResult();
            //1.准备request
            SearchRequest request = new SearchRequest("blog");
            //2.准备DSL
            //2.1.query
            BoolQueryBuilder boolQueryBuilder = new BoolQueryBuilder();
            String key = params.getKeyword();
            if(key == null || "".equals(key)){
                boolQueryBuilder.must(QueryBuilders.matchAllQuery());
            }else {
                boolQueryBuilder.must(QueryBuilders.matchQuery("all",key));
            }
            //2.2.根据分类查询
            if(params.getCategory() != null && !params.getCategory().equals("")){
                boolQueryBuilder.filter(QueryBuilders.termQuery("categoryList",params.getCategory()));
            }
            //2.3.算法控制（推荐博客置顶）
            FunctionScoreQueryBuilder functionScoreQueryBuilder = QueryBuilders.functionScoreQuery(
                    boolQueryBuilder,
                    new FunctionScoreQueryBuilder.FilterFunctionBuilder[]{
                            //其中一个具体的function score元素
                            new FunctionScoreQueryBuilder.FilterFunctionBuilder(
                                    //过滤条件
                                    QueryBuilders.termQuery("isRecommend",true),
                                    //算法函数
                                    ScoreFunctionBuilders.weightFactorFunction(10)
                            )
                    });
            request.source().query(functionScoreQueryBuilder);
            //2.4.分页
            int page = params.getPage();
            int size = params.getSize();
            request.source().from((page-1)*size).size(size);
            //2.5.排序
//            request.source().sort("time", SortOrder.DESC);
            //3.发送请求
            SearchResponse response = client.search(request, RequestOptions.DEFAULT);
            //4.解析response
            SearchHits searchHits = response.getHits();
            //4.1.获取总条数
            long total = searchHits.getTotalHits().value;
            //4.2.文档数据
            SearchHit[] hits = searchHits.getHits();
            List<BlogDoc> blogDocs = new ArrayList<>();
            for(SearchHit hit : hits){
                String json = hit.getSourceAsString();
                //反序列化
                BlogDoc blogDoc = JSON.parseObject(json,BlogDoc.class);
                blogDocs.add(blogDoc);
            }
            //4.3.封装返回
            pageResult.setBlogDocs(blogDocs);
            pageResult.setTotal(total);
            return new ResultVO(100,"成功",pageResult);
        } catch (IOException e) {
            throw new ServiceException("失败");
        }
    }

    /**
     * elasticsearch获取MQ进行新增和修改
     * @param id 博客Id
     */
    @Override
    public void saveBlogById(Integer id) {
        try {
            //1.根据id查询Blog
            SysBlog blog = getById(id);
            //2.转换为文件文档
            BlogDoc blogDoc = new BlogDoc(blog);
            //3.准备request
            IndexRequest request = new IndexRequest("blog").id(id.toString());
            //4.准备DSL
            request.source(JSON.toJSONString(blogDoc), XContentType.JSON);
            //5.发送请求
            client.index(request,RequestOptions.DEFAULT);
        } catch (IOException e) {
            throw new ServiceException("失败"+e);
        }
    }

    /**
     * elasticsearch获取MQ进行删除
     * @param id 博客Id
     */
    @Override
    public void deleteById(Integer id) {
        try {
            //1.准备request
            DeleteRequest request = new DeleteRequest("blog", id.toString());
            //2.准备发送请求
            client.delete(request,RequestOptions.DEFAULT);
        } catch (IOException e) {
            throw new ServiceException("失败"+e);
        }
    }

    /**
     * 当进行删除或修改时对缓存进行删除
     * @param key redis中的key
     */
    private void flushRedis(String key){
        stringRedisTemplate.delete(key);
    }
}




