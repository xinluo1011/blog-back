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
* @author ??????
* @description ????????????sys_blog?????????????????????Service??????
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
        //1.??????????????????
        Integer userId = TokenUtils.getCurrentUser().getId();
        //2.??????????????????????????????
        String key = BLOG_LIKED + blogId;
        boolean like = false;
        Double score = stringRedisTemplate.opsForZSet().score(key,userId.toString());
        if(score == null){
            //3.?????????
            //3.1?????????????????????
            Integer isSuccess = blogLikeMapper.insertLiked(userId,blogId);
            if(isSuccess != 0){
                stringRedisTemplate.opsForZSet().add(key,userId.toString(),System.currentTimeMillis());
                stringRedisTemplate.opsForHash().increment(BLOG_LIKED_KEY,blogId.toString(),1);
                like = false;
            }
        }else {
            //4.???????????????????????????
            //4.1??????????????????
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
            //?????????Id
            List<String> categoryList = blogCategoryMapper.getCategoryByBlogId(bId);
            //???????????????
            Integer liked = blogLikeMapper.getBlogLike(bId);
            //???????????????
            Integer star = blogMapper.getBlogStar(bId);
            try{
                //????????????????????????????????????
                //??????????????????
                Integer userId = TokenUtils.getCurrentUser().getId();
                Integer isLiked = blogLikeMapper.getIsLiked(bId,userId);
                //????????????????????????????????????
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
     * ????????????
     * @param blogStar ??????????????????
     * @return ResultVO????????????
     */
    @Override
    @Transactional
    public ResultVO starBlog(BlogStar blogStar) {
        //1.??????????????????Id
        Integer userId = TokenUtils.getCurrentUser().getId();
        //2.????????????Id?????????Id
        Integer blogId = blogStar.getBlogId();
        Integer groupId = blogStar.getGroupId();
        //3.????????????????????????????????????????????????????????????????????????
        if(groupId == null){
            blogStar.setGroupId(1);
        }else {
            //4.??????????????????????????????Id????????????
            StarGroup starGroup = blogMapper.getById(groupId);
            if(starGroup == null){
                //4.1?????????????????????
                throw new ServiceException("??????????????????");
            }
        }
        //5.????????????????????????
        if(blogId == null){
            //5.1????????????Id????????????
            throw new ServiceException("???????????????");
        }
        //5.2????????????????????????????????????
        SysBlog blog = blogMapper.getBlogById(blogId);
        if(blog == null){
            throw new ServiceException("???????????????");
        }
        //6.?????????????????????????????????
        blogMapper.deleteStar(blogId,userId);
        //7.??????????????????????????????
        blogStar.setUserId(userId);
        blogMapper.addStar(blogStar);
        stringRedisTemplate.delete(BLOG_KEY);
        stringRedisTemplate.opsForSet().add(BLOG_STAR+blogId,userId.toString());
        stringRedisTemplate.opsForHash().increment(BLOG_STAR_KEY,blogId.toString(),1);
        return new ResultVO(100,"??????",null);
    }

    /**
     * ????????????
     * @param blogId ??????Id
     * @param userId ??????Id
     * @return ResultVO ????????????
     */
    @Override
    public ResultVO DeleteStarBlog(Integer blogId,Integer userId) {
        blogMapper.deleteStar(blogId,userId);
        stringRedisTemplate.opsForSet().remove(BLOG_STAR+blogId,userId.toString());
        stringRedisTemplate.opsForHash().increment(BLOG_STAR_KEY,blogId.toString(),-1);
        return new ResultVO(100,"??????",null);
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
            return new ResultVO(ResStatus.ERROR,"???????????????????????????",null);
        }
        boolean save = saveOrUpdate(blog);
        if(save){
            flushRedis(BLOG_KEY);
            rabbitTemplate.convertAndSend(MqConstants.BLOG_EXCHANGE,MqConstants.BLOG_INSERT_KEY,blog.getId());
            return new ResultVO(ResStatus.SUCCESS,"??????", blog);
        }else {
            return new ResultVO(ResStatus.ERROR,"??????",null);
        }
    }

    @Override
    public ResultVO removeBlog(Integer id) {
        boolean remove = removeById(id);
        if(remove){
            flushRedis(BLOG_KEY);
            rabbitTemplate.convertAndSend(MqConstants.BLOG_EXCHANGE,MqConstants.BLOG_DELETE_KEY,id);
            return new ResultVO(ResStatus.SUCCESS,"??????", null);
        }else {
            return new ResultVO(ResStatus.ERROR,"??????",null);
        }
    }

    @Override
    public ResultVO removeBlogByIds(List<Integer> ids) {
        boolean remove = removeByIds(ids);
        if(remove){
            flushRedis(BLOG_KEY);
            return new ResultVO(ResStatus.SUCCESS,"??????", null);
        }else {
            return new ResultVO(ResStatus.ERROR,"??????",null);
        }
    }

    @Override
    public ResultVO findByPage(Integer pageNum, Integer pageSize, String name) {
        IPage<SysBlog> page = findPage(pageNum, pageSize, name);
        List<SysBlog> records = page.getRecords();
//        getCName(records);
        return new ResultVO(ResStatus.SUCCESS,"??????",page);
    }

    @Override
    public ResultVO findAll() {
        List<SysBlog> blogs;
        //1.?????????????????????
        String str = stringRedisTemplate.opsForValue().get(BLOG_KEY);
        //2.??????????????????
        if(StrUtil.isBlank(str)){
            //3.???????????????????????????
            blogs = findBlogs();
            for(SysBlog blog : blogs){
                List<String> categoryByBlogId = blogMapper.getCategoryByBlogId(blog.getId());
                blog.setCategoryList(categoryByBlogId);
            }
            //4.???????????????redis
            stringRedisTemplate.opsForValue().set(BLOG_KEY, JSONUtil.toJsonStr(blogs),1,TimeUnit.HOURS);
        }else {
            //5.???redis???????????????
            blogs = JSONUtil.toBean(str, new TypeReference<List<SysBlog>>(){},true);
        }
        //???????????????
        stringRedisTemplate.expire(BLOG_KEY,LOGIN_USER_TTL, TimeUnit.HOURS);
        return new ResultVO(ResStatus.SUCCESS,"??????",blogs);
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
        return new ResultVO(ResStatus.SUCCESS,"??????",blog);
    }

    @Override
    public ResultVO searchBlogES(RequestParams params) {
        try {
            PageResult pageResult = new PageResult();
            //1.??????request
            SearchRequest request = new SearchRequest("blog");
            //2.??????DSL
            //2.1.query
            BoolQueryBuilder boolQueryBuilder = new BoolQueryBuilder();
            String key = params.getKeyword();
            if(key == null || "".equals(key)){
                boolQueryBuilder.must(QueryBuilders.matchAllQuery());
            }else {
                boolQueryBuilder.must(QueryBuilders.matchQuery("all",key));
            }
            //2.2.??????????????????
            if(params.getCategory() != null && !params.getCategory().equals("")){
                boolQueryBuilder.filter(QueryBuilders.termQuery("categoryList",params.getCategory()));
            }
            //2.3.????????????????????????????????????
            FunctionScoreQueryBuilder functionScoreQueryBuilder = QueryBuilders.functionScoreQuery(
                    boolQueryBuilder,
                    new FunctionScoreQueryBuilder.FilterFunctionBuilder[]{
                            //?????????????????????function score??????
                            new FunctionScoreQueryBuilder.FilterFunctionBuilder(
                                    //????????????
                                    QueryBuilders.termQuery("isRecommend",true),
                                    //????????????
                                    ScoreFunctionBuilders.weightFactorFunction(10)
                            )
                    });
            request.source().query(functionScoreQueryBuilder);
            //2.4.??????
            int page = params.getPage();
            int size = params.getSize();
            request.source().from((page-1)*size).size(size);
            //2.5.??????
//            request.source().sort("time", SortOrder.DESC);
            //3.????????????
            SearchResponse response = client.search(request, RequestOptions.DEFAULT);
            //4.??????response
            SearchHits searchHits = response.getHits();
            //4.1.???????????????
            long total = searchHits.getTotalHits().value;
            //4.2.????????????
            SearchHit[] hits = searchHits.getHits();
            List<BlogDoc> blogDocs = new ArrayList<>();
            for(SearchHit hit : hits){
                String json = hit.getSourceAsString();
                //????????????
                BlogDoc blogDoc = JSON.parseObject(json,BlogDoc.class);
                blogDocs.add(blogDoc);
            }
            //4.3.????????????
            pageResult.setBlogDocs(blogDocs);
            pageResult.setTotal(total);
            return new ResultVO(100,"??????",pageResult);
        } catch (IOException e) {
            throw new ServiceException("??????");
        }
    }

    /**
     * elasticsearch??????MQ?????????????????????
     * @param id ??????Id
     */
    @Override
    public void saveBlogById(Integer id) {
        try {
            //1.??????id??????Blog
            SysBlog blog = getById(id);
            //2.?????????????????????
            BlogDoc blogDoc = new BlogDoc(blog);
            //3.??????request
            IndexRequest request = new IndexRequest("blog").id(id.toString());
            //4.??????DSL
            request.source(JSON.toJSONString(blogDoc), XContentType.JSON);
            //5.????????????
            client.index(request,RequestOptions.DEFAULT);
        } catch (IOException e) {
            throw new ServiceException("??????"+e);
        }
    }

    /**
     * elasticsearch??????MQ????????????
     * @param id ??????Id
     */
    @Override
    public void deleteById(Integer id) {
        try {
            //1.??????request
            DeleteRequest request = new DeleteRequest("blog", id.toString());
            //2.??????????????????
            client.delete(request,RequestOptions.DEFAULT);
        } catch (IOException e) {
            throw new ServiceException("??????"+e);
        }
    }

    /**
     * ????????????????????????????????????????????????
     * @param key redis??????key
     */
    private void flushRedis(String key){
        stringRedisTemplate.delete(key);
    }
}




