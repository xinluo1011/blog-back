package com.sit.manage.util;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.sit.manage.entity.SysUser;
import com.sit.manage.service.SysUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Date;

/**
 * @author 星络
 * @version 1.0
 * 生成token
 */
@Component
public class TokenUtils {

    private static SysUserService staticUserService;

    @Resource
    private SysUserService userService;

    @PostConstruct
    public void setUserService(){
        staticUserService = userService;
    }

    /**
     * 生成token
     * 根据userId查询用户数据
     * @return String
     */
    public static String tokenCreate(String userId){
        return JWT.create().withAudience(userId)//将userId保存到token里面作为载荷
                .withExpiresAt(DateUtil.offsetHour(new Date(),2))//设置token2小时有效
                .sign(Algorithm.HMAC256("MAO"+userId));//以password作为token的密钥
    }

    /**
     * 获取当前用户信息
     * @return user对象
     */
    public static SysUser getCurrentUser(){
        try{
            HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
            String token = request.getHeader("token");
            if(StrUtil.isNotBlank(token)){
                String userId = JWT.decode(token).getAudience().get(0);
                return staticUserService.getById(Integer.valueOf(userId));
            }
        }catch (Exception e){
            return null;
        }
        return null;
    }
}
