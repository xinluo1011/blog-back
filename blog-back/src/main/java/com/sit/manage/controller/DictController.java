package com.sit.manage.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.sit.manage.entity.SysDict;
import com.sit.manage.mapper.SysDictMapper;
import com.sit.manage.vo.ResStatus;
import com.sit.manage.vo.ResultVO;
import io.swagger.annotations.Api;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author 星络
 * @version 1.0
 */
@RestController
@RequestMapping("/dict")
@CrossOrigin
@Api(value = "图标管理", tags="图标管理")
public class DictController {

    @Resource
    private SysDictMapper sysDictMapper;


    @GetMapping("/icons")
    public ResultVO getIcons(){
        QueryWrapper<SysDict> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("type", ResStatus.DICT_TYPE_ICON);
        List<SysDict> sysDicts = sysDictMapper.selectList(queryWrapper);
        return new ResultVO(ResStatus.SUCCESS,"成功",sysDicts);
    }

}
