package com.sit.manage.entity;

import lombok.Data;

import java.util.List;

/**
 * @author 星络
 * @version 1.0
 */
@Data
public class PageResult {
    private Long total;
    private List<BlogDoc> blogDocs;
}
