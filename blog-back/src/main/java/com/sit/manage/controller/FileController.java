package com.sit.manage.controller;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.SecureUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.sit.manage.entity.SysFile;
import com.sit.manage.mapper.SysFileMapper;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import javax.annotation.Resource;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.net.URLEncoder;

/**
 * @author 星络
 * @version 1.0
 * 文件上传相关接口
 */
@RestController
@RequestMapping("/file")
@CrossOrigin
@Api(value = "文件上传", tags="文件上传")
public class FileController {

    @Value("${files.upload.path}")
    private String fileUploadPath;

    @Resource
    private SysFileMapper fileMapper;

    /**
     * 文件上传接口
     * @param file 前端传过来的文件
     * @return
     * @throws IOException
     */
    @PostMapping("/upload")
    @ApiOperation("上传文件接口")
    public String upload(@RequestParam MultipartFile file) throws IOException {
        String originalFilename = file.getOriginalFilename();//获取文件名
        String type = FileUtil.extName(originalFilename);//获取文件类型
        long size = file.getSize();//获取文件大小

        //定义一个文件唯一的标识码
        String uuid = IdUtil.fastSimpleUUID();
        String fileUUid =  uuid+ StrUtil.DOT + type;

        String saveURL;
        File uploadFile = new File(fileUploadPath + fileUUid);
        //如果路径中的父级目录不存在就创建一个文件
        if(!uploadFile.getParentFile().exists()){
            uploadFile.getParentFile().mkdirs();
        }

        SysFile saveFile = new SysFile();
        //获取文件的md5
        String md5 = SecureUtil.md5(file.getInputStream());
        SysFile files = getFileMd5(md5);
        //在数据库中中添加了md5的唯一索引保证唯一性
        //在数据库中根据md5查找是否已有文件，表示文件已经被保存在磁盘中过
        //如果已经存在则直接将数据库中的url返回
        //如果文件不存在则将文件存储到磁盘中
        if(files != null){
            saveURL = files.getUrl();
        }else {
            //把获取到的文件存储到磁盘
            file.transferTo(uploadFile);
            saveURL = "http://localhost:8082/file/"+fileUUid;
            saveFile.setMd5(md5);
        }

        //存储数据库
        saveFile.setName(originalFilename);
        saveFile.setType(type);
        saveFile.setSize(size/1024);
        saveFile.setUrl(saveURL);
        fileMapper.insert(saveFile);
        return saveURL;
    }

    /**
     * 通过文件的md5查询文件
     * @param md5 MD5
     * @return
     */
    private SysFile getFileMd5(String md5){
        //查询文件的md5是否存在
        QueryWrapper<SysFile> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("md5",md5);
        SysFile files = fileMapper.selectOne(queryWrapper);
        return files;
    }

    /**
     * 文件下载接口 "http://localhost:8082/file/"+{fileUUid}
     * @param file_uuid
     * @param response
     * @throws IOException
     */
    @GetMapping("/{file_uuid}")
    @ApiOperation("下载文件接口")
    public void download(@PathVariable String file_uuid, HttpServletResponse response) throws IOException {
        //根据文件的唯一表示码获取文件
        File uploadFile = new File(fileUploadPath + file_uuid);
        //设置输出流的格式
        ServletOutputStream os = response.getOutputStream();
        response.addHeader("Content-Disposition","attachment;filename="+ URLEncoder.encode(file_uuid,"UTF-8"));
        response.setContentType("application/octet-stream");

        //读取上传字节流
        os.write(FileUtil.readBytes(uploadFile));
        os.flush();
        os.close();
    }


}
