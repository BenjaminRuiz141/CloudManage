package com.duoc.CloudManage.aws.service.interfaces;

import java.io.IOException;
import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import com.duoc.CloudManage.aws.model.Asset;

public interface S3Service {

    String getS3FileContent(String key) throws IOException;

    List<Asset> getS3Files() throws IOException;

    byte[] downloadS3File(String key) throws IOException;

    void moveS3Object(String sourceKey, String destinationKey);

    void deleteS3Object(String key);

    String uploadS3Object(String key, MultipartFile file) throws IOException;

}
