package com.songyuankun.cloud.upload.controller;

import com.google.gson.Gson;
import com.qiniu.common.QiniuException;
import com.qiniu.common.Zone;
import com.qiniu.http.Response;
import com.qiniu.storage.Configuration;
import com.qiniu.storage.UploadManager;
import com.qiniu.storage.model.DefaultPutRet;
import com.qiniu.util.Auth;
import com.qiniu.util.StringMap;
import com.songyuankun.cloud.common.ResponseUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;

/**
 * @author songyuankun
 */

@Api(tags = "上传接口")
@RestController
@RequestMapping("upload")
public class UploadController {
    @Value("${qiniu.ak}")
    private String accessKey;
    @Value("${qiniu.sk}")
    private String secretKey;
    @Value("${qiniu.bucket}")
    private String bucket;


    @GetMapping("test")
    public void test() {
        //构造一个带指定Zone对象的配置类
        Configuration cfg = new Configuration(Zone.zone0());
//...其他参数参考类注释

        UploadManager uploadManager = new UploadManager(cfg);

        byte[] uploadBytes = "hello qiniu cloud".getBytes();
        ByteArrayInputStream byteInputStream = new ByteArrayInputStream(uploadBytes);
        Auth auth = Auth.create(accessKey, secretKey);
        String upToken = auth.uploadToken(bucket);

        try {
            Response response = uploadManager.put(byteInputStream, null, upToken, null, null);
            //解析上传成功的结果
            DefaultPutRet putRet = new Gson().fromJson(response.bodyString(), DefaultPutRet.class);
            System.out.println(putRet.key);
            System.out.println(putRet.hash);
        } catch (QiniuException ex) {
            Response r = ex.response;
            System.err.println(r.toString());
            try {
                System.err.println(r.bodyString());
            } catch (QiniuException ex2) {
                //ignore
            }
        }
    }

    @PostMapping("file")
    @ApiOperation(value = "上传文件", notes = "上传文件")
    public com.songyuankun.cloud.common.Response<String> upload(@ApiParam(name = "文件") @RequestParam("file") MultipartFile file) {
        Configuration cfg = new Configuration(Zone.zone0());

        UploadManager uploadManager = new UploadManager(cfg);

        Auth auth = Auth.create(accessKey, secretKey);
        String upToken = auth.uploadToken(bucket);

        Response response = null;
        try {
            response = uploadManager.put(file.getInputStream(), file.getOriginalFilename(), upToken, new StringMap(), null);
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            //解析上传成功的结果
            DefaultPutRet putRet = new Gson().fromJson(response != null ? response.bodyString() : "{}", DefaultPutRet.class);
            return ResponseUtils.success(putRet.key);
        } catch (QiniuException e) {
            e.printStackTrace();
        }
        return null;
    }
}
