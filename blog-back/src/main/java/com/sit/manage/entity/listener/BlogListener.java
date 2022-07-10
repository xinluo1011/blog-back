package com.sit.manage.entity.listener;

import com.sit.manage.service.SysBlogService;
import com.sit.manage.util.MqConstants;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author 星络
 * @version 1.0
 */
@Component
public class BlogListener {

    @Autowired
    private SysBlogService blogService;

    /**
     * 监听博客新增和修改业务
     */
    @RabbitListener(queues = MqConstants.BLOG_INSERT_QUEUE)
    public void listenerBlogInsertOrUpdate(Integer id){
        blogService.saveBlogById(id);
    }

    /**
     * 监听博客删除的业务
     */
    @RabbitListener(queues = MqConstants.BLOG_DELETE_QUEUE)
    public void listenerBlogDelete(Integer id){
        blogService.deleteById(id);
    }
}
