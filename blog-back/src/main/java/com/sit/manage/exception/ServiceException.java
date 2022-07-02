package com.sit.manage.exception;

import lombok.Getter;

/**
 * @author 星络
 * @version 1.0
 * 自定义异常
 */
@Getter
public class ServiceException extends RuntimeException{

    private int code;

    public ServiceException(int code, String msg){
        super(msg);
        this.code = code;
    }

}
