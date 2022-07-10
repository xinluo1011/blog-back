package com.sit.manage.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.sit.manage.controller.dto.PwdDTO;
import com.sit.manage.controller.dto.UserDTO;
import com.sit.manage.entity.SysUser;
import com.baomidou.mybatisplus.extension.service.IService;
import com.sit.manage.entity.UserFollow;
import com.sit.manage.entity.UserInfo;
import com.sit.manage.vo.ResultVO;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.List;

/**
* @author 星络
* @description 针对表【sys_user(用户表)】的数据库操作Service
* @createDate 2022-05-27 19:06:33
*/
public interface SysUserService extends IService<SysUser> {

    IPage<SysUser> findPage(Integer pageNum,Integer pageSize,String username,String email,String address);

    ResultVO loginByCode(UserDTO userDTO);

    ResultVO login(UserDTO userDTO);

    ResultVO code(UserDTO userDTO);

    ResultVO getUserInfo();

    ResultVO saveUserInfo(SysUser user);

    ResultVO editPwd(PwdDTO pwdDTO);

    SysUser findUserById(Integer id);

    List<UserFollow> findFollow(Integer pageNum, Integer pageSize, Integer userId);

    Boolean addFollow(UserFollow userFollow);

    List<UserInfo> findBothFollow(Integer userId,Integer followId);
}
