package com.sit.manage.entity;

import lombok.Data;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author 星络
 * @version 1.0
 */
@Data
public class BlogDoc {
    private Integer id;
    private String name;
//    private String content;
    private String description;
    private String user;
    private String tUrl;
    private String time;
    private Boolean isRecommend;
    private List<String> categoryList;

    public BlogDoc(SysBlog blog){
        this.id = blog.getId();
        this.name = blog.getName();
//        this.content = blog.getContent();
        this.description = blog.getDescription();
        this.user = blog.getUser();
        this.tUrl = blog.getTUrl();
        this.time = blog.getTime();
        this.categoryList = blog.getCategoryList();
        this.isRecommend = blog.getIsRecommend();
    }

    public BlogDoc(){}

}
