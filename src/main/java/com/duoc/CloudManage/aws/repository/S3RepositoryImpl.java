package com.duoc.CloudManage.aws.repository;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.CopyObjectRequest;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Response;
import software.amazon.awssdk.services.s3.model.S3Object;
import software.amazon.awssdk.services.s3.model.S3Exception;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.sync.RequestBody;

import com.duoc.CloudManage.aws.model.Asset;
import com.duoc.CloudManage.aws.repository.interfaces.S3Repository;

import java.util.stream.Collectors;

@Repository
public class S3RepositoryImpl implements S3Repository {

    @Value("${cloud.s3.bucket-name}")
    private String bucketName;

    private S3Client s3Client;

    public S3RepositoryImpl(S3Client s3Client) {
        this.s3Client = s3Client;
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(S3RepositoryImpl.class);

    @Override
    public List<Asset> listObjectsInBucket() {

        // Request 
        ListObjectsV2Request request = ListObjectsV2Request.builder()
        .bucket(bucketName)
        .build();

        ListObjectsV2Response response = s3Client.listObjectsV2(request);

        List<Asset> items = response.contents().stream()
                .parallel()
                .map(S3Object::key)
                .map(key -> mapS3ToObject(bucketName, key))
                .collect(Collectors.toList());                   

        LOGGER.info("Objects in bucket {}: {}", bucketName, items);

        return items;
    }

    public Asset mapS3ToObject(String bucketName, String key) {
        var headResponse = s3Client.headObject(HeadObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .build());

        return Asset.builder()
                .name(headResponse.metadata().get("name"))
                .key(key)
                .url(String.format("https://%s.s3.amazonaws.com/%s", bucketName, key))
                .build();
    }

    @Override
    public ResponseInputStream<GetObjectResponse> getObject(String key) {
        try {
            s3Client.headObject(HeadObjectRequest.builder().bucket(bucketName).key(key).build());
        } catch (S3Exception e) {
            LOGGER.warn("Object with key {} does not exist in bucket {}", key, bucketName);
            return null;
        }
        return s3Client.getObject(GetObjectRequest.builder().bucket(bucketName).key(key).build());
    }

    @Override
    public byte[] downloadObject(String key) throws IOException {
        try (ResponseInputStream<GetObjectResponse> inputStream = s3Client.getObject(
                GetObjectRequest.builder().bucket(bucketName).key(key).build())) {
            return inputStream.readAllBytes();
        } catch (IOException e) {
            LOGGER.error("Error downloading object with key {} from bucket {}: {}", key, bucketName, e.getMessage());
            throw e;
        }
    }

    @Override
    public void moveObject(String sourceKey, String destinationKey) {
        CopyObjectRequest copyObjRequest = CopyObjectRequest.builder()
            .sourceBucket(bucketName)
            .sourceKey(sourceKey)
            .destinationBucket(bucketName)
            .destinationKey(destinationKey)
            .build();
        s3Client.copyObject(copyObjRequest);
        deleteObject(sourceKey);
    }

    @Override
    public void deleteObject(String key) {
        s3Client.deleteObject
            (DeleteObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .build());
    }

    @Override
    public String uploadObject(String key, byte[] content) throws IOException {
        s3Client.putObject(
            PutObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .build(), 
            RequestBody.fromBytes(content));
        
        LOGGER.info("Bytes uploaded to bucket {} with key {}", bucketName, key);
        return key;
    }

    @Override
    public String uploadObject(String key, File file) throws IOException {
        s3Client.putObject
            (PutObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .build(), 
            RequestBody.fromFile(file.toPath()));
        file.delete();
        LOGGER.info("File {} uploaded to bucket {} with key {}", file.getName(), bucketName, key);
        return key;
    }

}
