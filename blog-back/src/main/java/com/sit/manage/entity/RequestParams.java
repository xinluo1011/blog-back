package com.sit.manage.entity;

import lombok.Data;

import java.util.List;

/**
 * @author 星络
 * @version 1.0
 * 全文搜索用户传来的请求参数
 */
@Data
public class RequestParams {
    private String keyword;
    private Integer page;
    private Integer size;
    private String sortBy;
    private String time;
    private String category;
    private Boolean isRecommend;
}
