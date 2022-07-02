package com.sit.manage.config.interceptor;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTDecodeException;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.sit.manage.controller.dto.UserDTO;
import com.sit.manage.entity.SysUser;
import com.sit.manage.exception.ServiceException;
import com.sit.manage.service.SysUserService;
import com.sit.manage.vo.ResStatus;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import static com.sit.manage.util.RedisConstants.LOGIN_USER_KEY;
import static com.sit.manage.util.RedisConstants.LOGIN_USER_TTL;

/**
 * @author 星络
 * @version 1.0
 * 拦截器
 */
public class JWTInterceptor implements HandlerInterceptor {

    SysUserService userService;

    private StringRedisTemplate stringRedisTemplate;

    public JWTInterceptor(SysUserService userService, StringRedisTemplate stringRedisTemplate) {
        this.userService = userService;
        this.stringRedisTemplate = stringRedisTemplate;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        //获取前端标头中的token
        String token = request.getHeader("token");

        //如果不是映射方法直接通过
        if (!(handler instanceof HandlerMethod)){
            return true;
        }
        //执行认证
        if(StrUtil.isBlank(token)){
            throw new ServiceException(ResStatus.ERROR, "无token,请重新登录！");
        }else {
            //基于token获取redis中的用户
            Map<Object, Object> userMap = stringRedisTemplate.opsForHash().entries(LOGIN_USER_KEY + token);
            //判断用户是否存在
            if(userMap.isEmpty()){
                throw new ServiceException(ResStatus.ERROR,"用户不存在！请重新登录");
            }
            //将查询到的Hash数据转为UserDTO对象
            UserDTO userDTO = BeanUtil.fillBeanWithMap(userMap, new UserDTO(), false);
            //获取token中的userId
            String userId;
            try{
                userId = JWT.decode(token).getAudience().get(0);
            }catch (JWTDecodeException e){
                throw new ServiceException(ResStatus.ERROR,"token验证失败,请重新登录！");
            }
            //根据token中的userId判断用户是否存在
            SysUser user = userService.getById(userId);
            if(user == null){
                throw new ServiceException(ResStatus.ERROR,"用户不存在！请重新登录");
            }
            //用户密码加签验证 token
            JWTVerifier jwtVerifier = JWT.require(Algorithm.HMAC256("MAO"+user.getId().toString())).build();
            try {
                jwtVerifier.verify(token);//验证token
            }catch (JWTVerificationException e){
                throw new ServiceException(ResStatus.ERROR,"token验证失败，请重新登录！");
            }
            //刷新token有效期
            stringRedisTemplate.expire(LOGIN_USER_KEY + token,LOGIN_USER_TTL, TimeUnit.HOURS);
        }
        return true;
    }
}
