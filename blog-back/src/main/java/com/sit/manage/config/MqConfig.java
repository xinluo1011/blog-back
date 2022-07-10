package com.sit.manage.config;

import com.sit.manage.util.MqConstants;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author 星络
 * @version 1.0
 */
@Configuration
public class MqConfig {

    //交换机
    @Bean
    public TopicExchange topicExchange(){
        return new TopicExchange(MqConstants.BLOG_EXCHANGE,true,false);
    }

    //新增或修改队列
    @Bean
    public Queue insertQueue(){
        return new Queue(MqConstants.BLOG_INSERT_QUEUE,true);
    }

    //删除队列
    @Bean
    public Queue deleteQueue(){
        return new Queue(MqConstants.BLOG_DELETE_QUEUE,true);
    }

    //新增或增加的绑定关系
    @Bean
    public Binding insertQueueBinding(){
        return BindingBuilder.bind(insertQueue()).to(topicExchange()).with(MqConstants.BLOG_INSERT_KEY);
    }

    //删除的绑定关系
    @Bean
    public Binding deleteQueueBinding(){
        return BindingBuilder.bind(deleteQueue()).to(topicExchange()).with(MqConstants.BLOG_DELETE_KEY);
    }

}
