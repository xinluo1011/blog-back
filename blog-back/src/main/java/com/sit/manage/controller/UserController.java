package com.sit.manage.controller;


import com.baomidou.mybatisplus.core.metadata.IPage;
import com.sit.manage.controller.dto.PwdDTO;
import com.sit.manage.controller.dto.UserDTO;
import com.sit.manage.entity.SysBlog;
import com.sit.manage.entity.SysUser;
import com.sit.manage.entity.UserFollow;
import com.sit.manage.entity.UserInfo;
import com.sit.manage.service.SysUserService;
import com.sit.manage.util.TokenUtils;
import com.sit.manage.vo.ResStatus;
import com.sit.manage.vo.ResultVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.*;
import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;



/**
 * @author 星络
 * @version 1.0
 */
@RestController
@RequestMapping("/user")
@CrossOrigin
@Api(value = "用户信息管理", tags="用户信息管理")
@Slf4j
public class UserController {

    @Resource
    private SysUserService userService;

    @ApiOperation("新增或修改用户信息")
    @PostMapping("/save")
    public ResultVO save(@RequestBody SysUser user){
        boolean save = userService.saveOrUpdate(user);
        if(save){
            return new ResultVO(ResStatus.SUCCESS,"成功", user);
        }else {
            return new ResultVO(ResStatus.ERROR,"失败",null);
        }
    }

    @ApiOperation("用户分页查询")
    @GetMapping("/page")
    public ResultVO findPage(@RequestParam Integer pageNum,
                                   @RequestParam Integer pageSize,
                                   @RequestParam(defaultValue = "") String username,
                                   @RequestParam(defaultValue = "") String email,
                                   @RequestParam(defaultValue = "") String address){
        IPage<SysUser> page = userService.findPage(pageNum, pageSize, username, email, address);
        return new ResultVO(ResStatus.SUCCESS,"成功",page);
    }

    @ApiOperation("删除用户")
    @DeleteMapping("/remove")
    public ResultVO remove(@RequestParam Integer id){
        boolean remove = userService.removeById(id);
        if(remove){
            return new ResultVO(ResStatus.SUCCESS,"成功", null);
        }else {
            return new ResultVO(ResStatus.ERROR,"失败",null);
        }
    }

    //批量删除多个用户
    @ApiOperation("批量删除用户")
    @PostMapping("/removeIds")
    public ResultVO removeByIds(@RequestBody List<Integer> ids){
        boolean remove = userService.removeByIds(ids);
        if(remove){
            return new ResultVO(ResStatus.SUCCESS,"成功", null);
        }else {
            return new ResultVO(ResStatus.ERROR,"失败",null);
        }
    }

    @ApiOperation("发送手机验证码")
    @PostMapping("/code")
    public ResultVO code(@RequestBody UserDTO userDTO){
        ResultVO register = userService.code(userDTO);
        return register;
    }

    @ApiOperation("手机验证码登录")
    @PostMapping("/loginCode")
    public ResultVO loginCode(@RequestBody UserDTO userDTO){
        ResultVO resultVO = userService.loginByCode(userDTO);
        return resultVO;
    }

    @ApiOperation("密码登录")
    @PostMapping("/login")
    public ResultVO login(@RequestBody UserDTO userDTO){
        ResultVO resultVO = userService.login(userDTO);
        return resultVO;
    }

    @ApiOperation("获取当前用户信息")
    @GetMapping("/info")
    public ResultVO getUserInfo(){
        ResultVO resultVO = userService.getUserInfo();
        return resultVO;
    }

    @ApiOperation("保存用户信息")
    @PostMapping("/save_info")
    public ResultVO saveUserInfo(@RequestBody SysUser user){
        ResultVO resultVO = userService.saveUserInfo(user);
        return resultVO;
    }

    @ApiOperation("修改密码")
    @PostMapping("/edit_pwd")
    public ResultVO editPwd(@RequestBody PwdDTO pwdDTO){
        ResultVO resultVO = userService.editPwd(pwdDTO);
        return resultVO;
    }

    @ApiOperation("通过id找用户")
    @GetMapping("/id/{id}")
    public ResultVO getUserById(@PathVariable Integer id){
        SysUser user = userService.getById(id);
        return new ResultVO(ResStatus.SUCCESS,"成功",user);
    }

    @ApiOperation("分页查询用户关注信息")
    @GetMapping("/follow")
    public ResultVO findFollow(@RequestParam Integer pageNum,
                             @RequestParam Integer pageSize){
        Integer userId = TokenUtils.getCurrentUser().getId();
        List<UserFollow> userFollows = userService.findFollow(pageNum,pageSize,userId);
        return new ResultVO(ResStatus.SUCCESS,"成功",userFollows);
    }

    @ApiOperation("关注用户")
    @PostMapping("/follow")
    public ResultVO addFollow(@RequestBody UserFollow userFollow){
        Boolean isSuccess = userService.addFollow(userFollow);
        return new ResultVO(100,"成功",isSuccess);
    }

    @ApiOperation("查询共同关注")
    @GetMapping("/both-follow")
    public ResultVO findBothFollow(@RequestParam Integer followId){
        Integer userId = TokenUtils.getCurrentUser().getId();
        List<UserInfo> userFollows = userService.findBothFollow(userId,followId);
        return new ResultVO(ResStatus.SUCCESS,"成功",userFollows);
    }
}
