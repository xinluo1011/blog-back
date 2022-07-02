package com.sit.manage.exception;

import com.sit.manage.vo.ResultVO;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;


/**
 * @author 星络
 * @version 1.0
 */
@ControllerAdvice
public class MyExceptionHandler {

    /**
     * 如果抛出的是ServiceException,则调用该方法
     * @param serviceException 业务异常
     * @return ResultVO
     */
    @ExceptionHandler(ServiceException.class)
    @ResponseBody
    public ResultVO handle(ServiceException serviceException){
        return new ResultVO(serviceException.getCode(), serviceException.getMessage(),null);
    }
}
