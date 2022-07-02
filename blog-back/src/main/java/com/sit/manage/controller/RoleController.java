package com.sit.manage.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.sit.manage.entity.SysRole;
import com.sit.manage.service.SysRoleService;
import com.sit.manage.vo.ResStatus;
import com.sit.manage.vo.ResultVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.models.auth.In;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.management.relation.Role;
import java.util.List;

/**
 * @author 星络
 * @version 1.0
 */
@RestController
@RequestMapping("/role")
@CrossOrigin
@Api(value = "权限管理", tags="权限管理")
public class RoleController {

    @Resource
    private SysRoleService roleService;

    //新增或更新
    @ApiOperation("新增或修改权限信息")
    @PostMapping("/save")
    public ResultVO save(@RequestBody SysRole role){
        boolean save = roleService.saveOrUpdate(role);
        if(save){
            return new ResultVO(ResStatus.SUCCESS,"成功", role);
        }else {
            return new ResultVO(ResStatus.ERROR,"失败",null);
        }

    }


    @ApiOperation("删除权限")
    @DeleteMapping("/remove")
    public ResultVO remove(@RequestParam Integer id){
        boolean remove = roleService.removeById(id);
        if(remove){
            return new ResultVO(ResStatus.SUCCESS,"成功", null);
        }else {
            return new ResultVO(ResStatus.ERROR,"失败",null);
        }
    }

    @ApiOperation("批量删除权限")
    @PostMapping("/removeIds")
    public ResultVO removeByIds(@RequestBody List<Integer> ids){
        boolean remove = roleService.removeByIds(ids);
        if(remove){
            return new ResultVO(ResStatus.SUCCESS,"成功", null);
        }else {
            return new ResultVO(ResStatus.ERROR,"失败",null);
        }
    }

    @ApiOperation("权限分页查询")
    @GetMapping("/page")
    public ResultVO findPage(@RequestParam Integer pageNum,
                             @RequestParam Integer pageSize,
                             @RequestParam(defaultValue = "") String name){
        IPage<SysRole> page = roleService.findPage(pageNum, pageSize, name);
        return new ResultVO(ResStatus.SUCCESS,"成功",page);
    }


    /**
     * 绑定角色和菜单的关系
     * @param roleId 角色ID
     * @param menuIds 菜单ID数组
     * @return ResultVO
     */
    @ApiOperation("权限菜单管理")
    @PostMapping("/role_menu/{roleId}")
    public ResultVO roleMenu(@PathVariable Integer roleId,@RequestBody List<Integer> menuIds){
        ResultVO resultVO = roleService.setRoleMenu(roleId, menuIds);
        return resultVO;
    }

    @ApiOperation("权限菜单管理")
    @GetMapping("/role_menu/{roleId}")
    public ResultVO getRoleMenu(@PathVariable Integer roleId){
        ResultVO resultVO = roleService.getRoleMenu(roleId);
        return resultVO;
    }

    @ApiOperation("权限管理")
    @GetMapping("/all")
    public ResultVO getRole(){
        List<SysRole> list = roleService.list();
        return new ResultVO(ResStatus.SUCCESS,"成功",list);
    }

    @ApiOperation("权限管理")
    @GetMapping("/all/{role}")
    public ResultVO getRoleById(@PathVariable Integer role){
        QueryWrapper<SysRole> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("id",role);
        SysRole one = roleService.getOne(queryWrapper);
        return new ResultVO(ResStatus.SUCCESS,"成功",one);
    }

}
