package com.sit.manage.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.RandomUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sit.manage.controller.dto.PwdDTO;
import com.sit.manage.controller.dto.UserDTO;
import com.sit.manage.entity.SysMenu;
import com.sit.manage.entity.SysRole;
import com.sit.manage.entity.SysUser;
import com.sit.manage.exception.ServiceException;
import com.sit.manage.mapper.RoleMenuMapper;
import com.sit.manage.mapper.SysRoleMapper;
import com.sit.manage.service.SysMenuService;
import com.sit.manage.service.SysUserService;
import com.sit.manage.mapper.SysUserMapper;
import com.sit.manage.util.MD5Utils;
import com.sit.manage.util.RegexUtils;
import com.sit.manage.util.TokenUtils;
import com.sit.manage.vo.ResStatus;
import com.sit.manage.vo.ResultVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PathVariable;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static com.sit.manage.util.RedisConstants.*;
import static com.sit.manage.util.SystemConstants.USER_NICK_NAME_PREFIX;


/**
* @author 星络
* @description 针对表【sys_user(用户表)】的数据库操作Service实现
* @createDate 2022-05-27 19:06:33
*/
@Service
@SuppressWarnings({"all"})
public class SysUserServiceImpl extends ServiceImpl<SysUserMapper, SysUser>
    implements SysUserService{

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private SysRoleMapper roleMapper;

    @Resource
    private RoleMenuMapper roleMenuMapper;

    @Resource
    private SysMenuService menuService;

    @Autowired
    SysUserMapper userMapper;

    //分页查询
    @Override
    public IPage<SysUser> findPage(Integer pageNum, Integer pageSize, String username, String email, String address) {
        IPage<SysUser> page = new Page<>(pageNum,pageSize);
        QueryWrapper<SysUser> wrapper = new QueryWrapper<>();
        wrapper.orderByDesc("id");
        if(!"".equals(username)) wrapper.like("username",username);
        if(!"".equals(email)) wrapper.like("email",email);
        if(!"".equals(address))wrapper.like("address",address);
        return page(page, wrapper);
    }

    //使用手机号和验证码登录
    @Override
    public ResultVO loginByCode(UserDTO userDTO) {
        String phone = userDTO.getPhone();
        String code = userDTO.getCode();
        //校验手机号
        if(RegexUtils.isPhoneInvalid(phone)){
            //如果不符合，返回错误信息
            return new ResultVO(ResStatus.ERROR,"手机号格式错误",null);
        }
        //从redis获取并进行校验验证码
        String cacheCode = stringRedisTemplate.opsForValue().get(LOGIN_CODE_KEY + phone);
        if(cacheCode == null || !cacheCode.equals(code)){
            //不一致报错
            return new ResultVO(ResStatus.ERROR,"验证码错误",null);
        }
        //一致根据手机号查询用户
        SysUser user = query().eq("phone", phone).one();
        //判断用户是否存在
        if(user == null){
            //不存在,创建新用户
            user = createUserWithPhone(phone);
            user.setId(user.getId());
        }

        //保存用户信息到redis中
        //设置token
        String token = TokenUtils.tokenCreate(user.getId().toString());
        userDTO.setToken(token);

        Integer role = user.getRole();
        //查找用户权限对应的菜单表
        List<SysMenu> roleMenu = getRoleMenu(role);
        userDTO.setMenus(roleMenu);
        //将User对象转为HashMap存储
        BeanUtil.copyProperties(user,userDTO,true);//将user中的属性copy给userDTO中并忽略大小写
        userDTO.setUserId(user.getId().toString());
        BeanUtil.copyProperties(user,userDTO,true);//将user中的属性copy给userDTO中并忽略大小写
        //将User对象转为HashMap存储
        Map<String, Object> userMap = new HashMap<>();
        userMap.put("username",userDTO.getUsername());
        userMap.put("userId",userDTO.getUserId());
        userMap.put("phone",userDTO.getPhone());
        userMap.put("password",userDTO.getPassword());
        userMap.put("code",userDTO.getCode());
        userMap.put("avatarUrl",userDTO.getAvatarUrl());
        userMap.put("menus",userDTO.getMenus().toString());
        userMap.put("nickName",userDTO.getNickname());
        userMap.put("role",userDTO.getRole().toString());
        userMap.put("token",userDTO.getToken());
        //将user信息转为HashMap，以便存入redis中
        //存储
        stringRedisTemplate.opsForHash().putAll(LOGIN_USER_KEY+token,userMap);
        //设置有效期
        stringRedisTemplate.expire(LOGIN_USER_KEY+token,LOGIN_USER_TTL,TimeUnit.HOURS);


        return new ResultVO(ResStatus.SUCCESS,"登录成功",userDTO);
    }

    private SysUser createUserWithPhone(String phone) {
        //创建用户
        SysUser user = new SysUser();
        user.setRole(2);
        user.setPhone(phone);
        user.setNickname("坛友"+RandomUtil.randomString(5));
        user.setAvatarUrl("https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png");
        user.setUsername(USER_NICK_NAME_PREFIX+RandomUtil.randomString(10));
        //保存用户
        save(user);
        return user;
    }

    //使用密码登录
    @Override
    public ResultVO login(UserDTO userDTO) {
        String username = userDTO.getUsername();
        String password = userDTO.getPassword();
        //判断输入框是否为空
        if(username == null || password == null){
            return new ResultVO(ResStatus.ERROR,"请输入正确的登录信息",null);
        }
        String md5 = MD5Utils.md5(password);
        //判断是否是以手机号形式登录->判断输入的格式是否符合手机号格式
        if(RegexUtils.isPhoneInvalid(username)){
            //如果不符合，则就是用用户账号登录
            //判断输入的用户账号是否在数据库中
            QueryWrapper<SysUser> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("username",username);
            queryWrapper.eq("password",md5);
            SysUser user;
            try{
                user = getOne(queryWrapper);
                user.setId(user.getId());
            }catch (Exception e){
                throw new ServiceException(ResStatus.ERROR,"此账号没有设置过密码，登录失败");
            }
            if (user != null){
                BeanUtil.copyProperties(user,userDTO,true);//将user中的属性copy给userDTO中并忽略大小写
                userDTO.setUserId(user.getId().toString());
                String token = TokenUtils.tokenCreate(user.getId().toString());
                userDTO.setToken(token);

                Integer role = user.getRole();
                //查找用户权限对应的菜单表
                List<SysMenu> roleMenu = getRoleMenu(role);
                userDTO.setMenus(roleMenu);


                BeanUtil.copyProperties(user,userDTO,true);//将user中的属性copy给userDTO中并忽略大小写
                //将User对象转为HashMap存储
                Map<String, Object> userMap = new HashMap<>();
                userMap.put("username",userDTO.getUsername());
                userMap.put("userId",userDTO.getUserId());
                userMap.put("phone",userDTO.getPhone());
                userMap.put("password",userDTO.getPassword());
                userMap.put("code",userDTO.getCode());
                userMap.put("avatarUrl",userDTO.getAvatarUrl());
                userMap.put("menus",userDTO.getMenus().toString());
                userMap.put("nickName",userDTO.getNickname());
                userMap.put("role",userDTO.getRole().toString());
                userMap.put("token",userDTO.getToken());
                //将user信息转为HashMap，以便存入redis中
                //存储
                stringRedisTemplate.opsForHash().putAll(LOGIN_USER_KEY+token,userMap);
                //设置有效期
                stringRedisTemplate.expire(LOGIN_USER_KEY+token,LOGIN_USER_TTL,TimeUnit.HOURS);

                return new ResultVO(ResStatus.SUCCESS,"登录成功",userDTO);
            }else {
                throw new ServiceException(ResStatus.ERROR,"用户名或密码错误");
            }
        }
        //如果符合就是以手机号登录
        QueryWrapper<SysUser> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("phone",username);
        queryWrapper.eq("password",md5);
        SysUser user;
        try{
            user = getOne(queryWrapper);
            user.setId(user.getId());
        }catch (Exception e){
            return new ResultVO(ResStatus.ERROR,"此账号没有设置过密码，登录失败",null);
        }
        if(user!=null){
            BeanUtil.copyProperties(user,userDTO,true);//将user中的属性copy给userDTO中并忽略大小写
            String token = TokenUtils.tokenCreate(user.getId().toString());
            userDTO.setToken(token);

            Integer role = user.getRole();
            //查找用户权限对应的菜单表
            List<SysMenu> roleMenu = getRoleMenu(role);
            userDTO.setMenus(roleMenu);

            //将User对象转为HashMap存储
            BeanUtil.copyProperties(user,userDTO,true);//将user中的属性copy给userDTO中并忽略大小写
            userDTO.setUserId(user.getId().toString());
            BeanUtil.copyProperties(user,userDTO,true);//将user中的属性copy给userDTO中并忽略大小写
            //将User对象转为HashMap存储
            Map<String, Object> userMap = new HashMap<>();
            userMap.put("username",userDTO.getUsername());
            userMap.put("userId",userDTO.getUserId());
            userMap.put("phone",userDTO.getPhone());
            userMap.put("password",userDTO.getPassword());
            userMap.put("code",userDTO.getCode());
            userMap.put("avatarUrl",userDTO.getAvatarUrl());
            userMap.put("menus",userDTO.getMenus().toString());
            userMap.put("nickName",userDTO.getNickname());
            userMap.put("role",userDTO.getRole().toString());
            userMap.put("token",userDTO.getToken());
            //将user信息转为HashMap，以便存入redis中
            //存储
            stringRedisTemplate.opsForHash().putAll(LOGIN_USER_KEY+token,userMap);
            //设置有效期
            stringRedisTemplate.expire(LOGIN_USER_KEY+token,LOGIN_USER_TTL,TimeUnit.HOURS);
            return new ResultVO(ResStatus.SUCCESS,"登录成功",userDTO);
        }else {
            throw new ServiceException(ResStatus.ERROR,"手机号或密码错误");
        }
    }

    //发送手机验证码
    @Override
    public ResultVO code(UserDTO userDTO) {
        String phone = userDTO.getPhone();
        //校验手机号
        if(RegexUtils.isPhoneInvalid(phone)){
            //如果不符合，返回错误信息
            return new ResultVO(ResStatus.ERROR,"手机号格式错误",null);
        }
        //符合，则生成验证码
        String code = RandomUtil.randomNumbers(6);//生成6位的随机数字

        //保存验证码存入redis=>key:手机号 value:验证码 设置验证码的有效时间
        stringRedisTemplate.opsForValue().set(LOGIN_CODE_KEY+phone,code,LOGIN_CODE_TTL, TimeUnit.MINUTES);

        //发送验证码
        System.out.println(code);

        //返回ok
        return new ResultVO(ResStatus.SUCCESS,"成功",null);
    }

    @Override
    public ResultVO getUserInfo() {
        SysUser user = TokenUtils.getCurrentUser();
        return new ResultVO(ResStatus.SUCCESS,"成功",user);
    }

    @Override
    public ResultVO saveUserInfo(SysUser user) {
        //判断邮箱，昵称是否重复
        SysUser info = userMapper.checkNickNameSaveInfo(user);
        if(info != null){
            return new ResultVO(ResStatus.ERROR,"昵称已被使用",null);
        }
        info = userMapper.checkSaveInfo(user);
        if(info != null){
            return new ResultVO(ResStatus.ERROR,"邮箱已被使用",null);
        }
        boolean b = saveOrUpdate(user);
        if(b){
            return new ResultVO(ResStatus.SUCCESS,"成功", user);
        }else {
            return new ResultVO(ResStatus.ERROR,"失败",null);
        }
    }

    @Override
    public ResultVO editPwd(PwdDTO pwdDTO) {
        if(pwdDTO.getPassword()==null) return new ResultVO(ResStatus.ERROR,"密码不为空",null);
        //曾经设置过密码
        if(pwdDTO.getRole() == 1){
            if(pwdDTO.getNewPassword()==null || pwdDTO.getPreNewpassword() == null) return new ResultVO(ResStatus.ERROR,"新密码不为空",null);
            QueryWrapper<SysUser> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("id",pwdDTO.getId());
            String md5 = MD5Utils.md5(pwdDTO.getPassword());
            queryWrapper.eq("password",md5);
            SysUser user = getOne(queryWrapper);
            if(user == null){
                return new ResultVO(ResStatus.ERROR,"原密码不正确",null);
            }
            if(!pwdDTO.getNewPassword().equals(pwdDTO.getPreNewpassword())){
                return new ResultVO(ResStatus.ERROR,"密码校验不正确",null);
            }
            md5=MD5Utils.md5(pwdDTO.getNewPassword());
            user.setPassword(md5);
            boolean b = updateById(user);
            if(b){
                return new ResultVO(ResStatus.SUCCESS,"成功",user);
            }else {
                return new ResultVO(ResStatus.ERROR,"失败",null);
            }
        }
        //曾经没有设置过密码
        else {
            if (pwdDTO.getPreNewpassword()==null) return new ResultVO(ResStatus.ERROR,"密码不为空",null);
            QueryWrapper<SysUser> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("id",pwdDTO.getId());
            SysUser user = getOne(queryWrapper);
            if(user == null){
                return new ResultVO(ResStatus.ERROR,"失败",null);
            }
            if(!pwdDTO.getPassword().equals(pwdDTO.getPreNewpassword())){
                return new ResultVO(ResStatus.ERROR,"密码校验不正确",null);
            }
            String md5 = MD5Utils.md5(pwdDTO.getPassword());
            user.setPassword(md5);
            boolean b = updateById(user);
            if(b){
                return new ResultVO(ResStatus.SUCCESS,"成功",user);
            }else {
                return new ResultVO(ResStatus.ERROR,"失败",null);
            }
        }
    }

    @Override
    public SysUser findUserById(Integer id) {
        SysUser userById = userMapper.findUserById(id);
        return userById;
    }

    /**
     * 获取当前角色的菜单列表
     * @param role
     * @return
     */
    private List<SysMenu> getRoleMenu(Integer role){
        List<Integer> menuIds = roleMenuMapper.selectByRoleId(role);
        List<SysMenu> menus = (List<SysMenu>) menuService.findMenus("").getData();
        //筛选出的菜单
        List<SysMenu> roleMenu = new ArrayList<>();
        //筛选当前用户角色的菜单
        for (SysMenu sysMenu : menus) {
            if(menuIds.contains(sysMenu.getId())){
                roleMenu.add(sysMenu);
            }
            List<SysMenu> children = sysMenu.getChildren();
            children.removeIf(child ->!menuIds.contains(child.getId()));
        }
        return roleMenu;
    }
}




