package com.sit.manage.controller.dto;

import lombok.Data;

/**
 * @author 星络
 * @version 1.0
 */
@Data
public class PwdDTO {
    private String password;
    private String newPassword;
    private String preNewpassword;
    private Integer role;
    private Integer id;
}
