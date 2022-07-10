package com.sit.manage.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import lombok.Data;

/**
 * 博客收藏
 * @TableName blog_star
 */
@TableName(value ="blog_star")
@Data
public class BlogStar implements Serializable {
    /**
     * 
     */
    @TableId(type = IdType.AUTO)
    private Integer id;

    /**
     * 
     */
    private Integer blogId;

    /**
     * 
     */
    private Integer userId;


    private Integer groupId;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;

    }