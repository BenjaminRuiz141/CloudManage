package com.duoc.CloudManage.aws.service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.duoc.CloudManage.aws.model.Asset;
import com.duoc.CloudManage.aws.repository.interfaces.S3Repository;
import com.duoc.CloudManage.aws.service.interfaces.S3Service;

@Service
public class S3ServiceImpl implements S3Service {

    private static final Logger LOGGER = LoggerFactory.getLogger(S3ServiceImpl.class);

    private S3Repository s3Repository;

    public S3ServiceImpl(S3Repository s3Repository) {
        this.s3Repository = s3Repository;
    }

    @Override
    public String getS3FileContent(String key) throws IOException {
        return new String(s3Repository.downloadObject(key), StandardCharsets.UTF_8);
    }

    @Override
    public List<Asset> getS3Files() throws IOException {
        return s3Repository.listObjectsInBucket();
    }

    @Override
    public byte[] downloadS3File(String key) throws IOException {
        return s3Repository.downloadObject(key);
    }

    @Override
    public void moveS3Object(String sourceKey, String destinationKey) {
        s3Repository.moveObject(sourceKey, destinationKey);
    }

    @Override
    public void deleteS3Object(String key) {
        s3Repository.deleteObject(key);
    }

    // Nuevo método para subir archivos directamente desde el disco (EFS)
    public String uploadS3File(String key, File file) throws IOException {
        return s3Repository.uploadObject(key, file);
    }

    public String uploadS3File(String key, byte[] content) throws IOException {
        return s3Repository.uploadObject(key, content);
    }

    @Override
    public String uploadS3Object(String key, MultipartFile file) throws IOException {
        return s3Repository.uploadObject(key, convertMultipartFileToFile(file));
    }

    private File convertMultipartFileToFile(MultipartFile file) throws IOException {
        File convertedFile = new File(file.getOriginalFilename());
        try (FileOutputStream fos = new FileOutputStream(convertedFile)) {
            fos.write(file.getBytes());
        } catch (IOException e) {
            LOGGER.error("Error converting MultipartFile to File: {}", e.getMessage());
            throw e;
        }
        return convertedFile;
    }

}
