package com.sit.manage.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.sit.manage.entity.SysMenu;
import com.sit.manage.service.SysMenuService;
import com.sit.manage.vo.ResStatus;
import com.sit.manage.vo.ResultVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;


/**
 * @author 星络
 * @version 1.0
 */
@RestController
@RequestMapping("/menu")
@CrossOrigin
@Api(value = "菜单管理", tags="菜单管理")
public class MenuController {

    @Resource
    SysMenuService menuService;

    //新增或更新
    @ApiOperation("新增或修改菜单信息")
    @PostMapping("/save")
    public ResultVO save(@RequestBody SysMenu menu){
        boolean save = menuService.saveOrUpdate(menu);
        if(save){
            return new ResultVO(ResStatus.SUCCESS,"成功", menu);
        }else {
            return new ResultVO(ResStatus.ERROR,"失败",null);
        }

    }

    @ApiOperation("删除菜单")
    @DeleteMapping("/remove")
    public ResultVO remove(@RequestParam Integer id){
        boolean remove = menuService.removeById(id);
        if(remove){
            return new ResultVO(ResStatus.SUCCESS,"成功", null);
        }else {
            return new ResultVO(ResStatus.ERROR,"失败",null);
        }
    }

    @ApiOperation("批量删除菜单")
    @PostMapping("/removeIds")
    public ResultVO removeByIds(@RequestBody List<Integer> ids){
        boolean remove = menuService.removeByIds(ids);
        if(remove){
            return new ResultVO(ResStatus.SUCCESS,"成功", null);
        }else {
            return new ResultVO(ResStatus.ERROR,"失败",null);
        }
    }


    @ApiOperation("菜单分页查询")
    @GetMapping("/page")
    public ResultVO findPage(@RequestParam Integer pageNum,
                             @RequestParam Integer pageSize,
                             @RequestParam(defaultValue = "") String name){
        IPage<SysMenu> page = menuService.findPage(pageNum, pageSize, name);
        return new ResultVO(ResStatus.SUCCESS,"成功",page);
    }

    @ApiOperation("权限菜单")
    @GetMapping("/all")
    public ResultVO findAll(@RequestParam(defaultValue = "") String name){

        ResultVO menus = menuService.findMenus(name);
        return menus;

    }

}
