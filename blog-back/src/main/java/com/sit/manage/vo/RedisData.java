package com.sit.manage.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * @author 星络
 * @version 1.0
 */
@Data
public class RedisData {
    private LocalDateTime expireTime;
    private Object data;
}
