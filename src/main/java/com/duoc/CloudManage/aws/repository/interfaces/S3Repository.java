package com.duoc.CloudManage.aws.repository.interfaces;

import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import com.duoc.CloudManage.aws.model.Asset;

import java.io.File;
import java.io.IOException;
import java.util.List;

public interface S3Repository {

    List<Asset> listObjectsInBucket ();

    ResponseInputStream<GetObjectResponse> getObject(String key);

    byte[] downloadObject(String key) throws IOException;

    void moveObject(String sourceKey, String destinationKey);

    void deleteObject(String key);

    String uploadObject(String key, File file) throws IOException;

    String uploadObject(String key, byte[] content) throws IOException;
}
