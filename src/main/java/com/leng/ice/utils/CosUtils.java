package com.leng.ice.utils;

import com.leng.ice.exception.BusinessException;
import com.leng.ice.common.ErrorCode;
import com.qcloud.cos.COSClient;
import com.qcloud.cos.ClientConfig;
import com.qcloud.cos.auth.BasicCOSCredentials;
import com.qcloud.cos.auth.COSCredentials;
import com.qcloud.cos.model.ObjectMetadata;
import com.qcloud.cos.model.PutObjectRequest;
import com.qcloud.cos.region.Region;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.UUID;

/**
 * 腾讯云COS工具类
 */
@Component
public class CosUtils {

    @Value("${cos.client.accessKey}")
    private String secretId;

    @Value("${cos.client.secretKey}")
    private String secretKey;

    @Value("${cos.client.region}")
    private String region;

    @Value("${cos.client.bucket}")
    private String bucketName;

    @Value("${file.cos.host}")
    private String cosHost;

    private COSClient cosClient;

    /**
     * 初始化
     */
    @PostConstruct
    public void init() {
        // 初始化COS客户端
        COSCredentials cred = new BasicCOSCredentials(secretId, secretKey);
        ClientConfig clientConfig = new ClientConfig(new Region(region));
        cosClient = new COSClient(cred, clientConfig);
    }

    /**
     * 上传文件到COS
     * @param file 文件
     * @param fileDir 文件目录
     * @return 文件访问路径
     */
    public String uploadFile(MultipartFile file, String fileDir) {
        try {
            // 生成文件名
            String originalFilename = file.getOriginalFilename();
            String suffix = originalFilename.substring(originalFilename.lastIndexOf("."));
            String fileName = UUID.randomUUID().toString().replaceAll("-", "") + suffix;
            String key = fileDir + "/" + fileName;

            // 设置文件元数据
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentLength(file.getSize());
            metadata.setContentType(file.getContentType());

            // 上传文件
            PutObjectRequest putObjectRequest = new PutObjectRequest(bucketName, key, file.getInputStream(), metadata);
            cosClient.putObject(putObjectRequest);

            // 返回文件访问路径
            return cosHost + "/" + key;
        } catch (IOException e) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "文件上传失败");
        }
    }
}