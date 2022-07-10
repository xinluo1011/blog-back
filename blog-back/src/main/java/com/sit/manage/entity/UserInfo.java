package com.sit.manage.entity;

import com.sit.manage.controller.dto.UserDTO;
import lombok.Data;

import java.util.List;

/**
 * @author 星络
 * @version 1.0
 */
@Data
public class UserInfo {
    private Integer userId;
    private String avatarUrl;
    private String nickname;
}
