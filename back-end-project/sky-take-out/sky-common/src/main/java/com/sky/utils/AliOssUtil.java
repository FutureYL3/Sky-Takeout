package com.sky.utils;


import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.OSSException;
import com.sky.properties.AliOssProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.UUID;

@Data
@Slf4j
@Component
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class AliOssUtil {
    private final AliOssProperties aliOSSProperties;

    public String upload(MultipartFile file) {
        String endpoint = aliOSSProperties.getEndpoint();
        String accessKeyId = aliOSSProperties.getAccessKeyId();
        String accessKeySecret = aliOSSProperties.getAccessKeySecret();
        String bucketName = aliOSSProperties.getBucketName();

        String originalFilename = file.getOriginalFilename();
        // 构造唯一文件名——加入 uuid（通用唯一识别码）
        int index = originalFilename.lastIndexOf('.');
        String extname = originalFilename.substring(index);
        String fileName = UUID.randomUUID().toString() + extname;

        // 创建 OSSClient 实例
        OSS ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);
        try {
            InputStream inputStream = file.getInputStream();
            ossClient.putObject(bucketName, fileName, inputStream);
        } catch (OSSException oe) {
            System.out.println("Caught an OSSException, which means your request made it to OSS, "
                    + "but was rejected with an error response for some reason.");
            System.out.println("Error Message:" +
                    oe.getErrorMessage());
            System.out.println("Error Code:" +
                    oe.getErrorCode());
            System.out.println("Request ID:" +
                    oe.getRequestId());
            System.out.println("Host ID:" + oe.getHostId());
        } catch (Exception ce) {
            System.out.println("Caught an ClientException, which " +
                    "means the client encountered "
                    + "a serious internal problem while trying to " +
                    "communicate with OSS, "
                    + "such as not being able to access the " +
                    "network.");
            System.out.println("Error Message:" + ce.getMessage());
        } finally {
            if (ossClient != null) {
                ossClient.shutdown();
            }
        }
        return endpoint.split("//")[0] + "//" + bucketName + "." + endpoint.split("//")[1] + "/" + fileName;
    }
}
