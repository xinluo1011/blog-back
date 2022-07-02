package com.sit.manage.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.sit.manage.entity.SysBlog;
import com.sit.manage.entity.SysCategory;
import com.sit.manage.entity.SysUser;
import com.sit.manage.service.SysCategoryService;
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
@RequestMapping("/category")
@CrossOrigin
@Api(value = "分类管理", tags="分类管理")
public class CategoryController {

    @Resource
    SysCategoryService categoryService;

    @ApiOperation("新增或修改分类信息")
    @PostMapping("/save")
    public ResultVO save(@RequestBody SysCategory category){
        boolean save = categoryService.saveOrUpdate(category);
        if(save){
            return new ResultVO(ResStatus.SUCCESS,"成功", category);
        }else {
            return new ResultVO(ResStatus.ERROR,"失败",null);
        }
    }

    @ApiOperation("分类分页查询")
    @GetMapping("/page")
    public ResultVO findPage(@RequestParam Integer pageNum,
                             @RequestParam Integer pageSize,
                             @RequestParam(defaultValue = "") String name){
        IPage<SysCategory> page = categoryService.findPage(pageNum, pageSize, name);

        return new ResultVO(ResStatus.SUCCESS,"成功",page);
    }

    @ApiOperation("删除分类")
    @DeleteMapping("/remove")
    public ResultVO remove(@RequestParam Integer id){
        boolean remove = categoryService.removeById(id);
        if(remove){
            return new ResultVO(ResStatus.SUCCESS,"成功", null);
        }else {
            return new ResultVO(ResStatus.ERROR,"失败",null);
        }
    }

    @ApiOperation("批量删除用户")
    @PostMapping("/removeIds")
    public ResultVO removeByIds(@RequestBody List<Integer> ids){
        boolean remove = categoryService.removeByIds(ids);
        if(remove){
            return new ResultVO(ResStatus.SUCCESS,"成功", null);
        }else {
            return new ResultVO(ResStatus.ERROR,"失败",null);
        }
    }

    @ApiOperation("全部分类")
    @GetMapping("/all")
    public ResultVO findAll(){
        List<SysCategory> list = categoryService.list();
        return new ResultVO(ResStatus.SUCCESS,"成功",list);
    }


}
