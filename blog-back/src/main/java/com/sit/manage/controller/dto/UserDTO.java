package com.sit.manage.controller.dto;

import com.sit.manage.entity.SysMenu;
import lombok.Data;

import java.awt.*;
import java.util.List;

/**
 * @author 星络
 * @version 1.0
 * 接收前端登录请求参数
 */
@Data
public class UserDTO {
    private String userId;
    private String avatarUrl;
    private String username;
    private String nickname;
    private String password;
    private String phone;
    private String code;
    private String token;
    private Integer role;
    private List<SysMenu> menus;
}
