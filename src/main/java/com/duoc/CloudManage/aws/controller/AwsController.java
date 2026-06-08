package com.duoc.CloudManage.aws.controller;

import java.io.IOException;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.duoc.CloudManage.aws.service.interfaces.S3Service;

import org.springframework.web.multipart.MultipartFile;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PostMapping;




@RestController
@RequestMapping("/s3")
public class AwsController {

    private S3Service awsService;

    public AwsController(S3Service awsService) {
        this.awsService = awsService;
    }
    
    // GET /s3/getS3FileContent - Obtiene el contenido de un archivo de S3 como texto
    @GetMapping("/getS3FileContent")
    public ResponseEntity<String> getS3FileContent(@RequestParam String bucketName, @RequestParam String key) throws IOException{
        String content = awsService.getS3FileContent(key);
        return ResponseEntity.ok(content);
    }
    
    // GET /s3/downloadS3File - Descarga un archivo de S3 como arreglo de bytes
    @GetMapping("/downloadS3File")
    public ResponseEntity<byte[]> downloadS3File(@RequestParam String bucketName, @RequestParam String key) throws IOException {
        byte[] fileContent = awsService.downloadS3File(key);
        return ResponseEntity.ok().body(fileContent);
    }

    // DELETE /s3/deleteObject - Elimina un objeto de S3
    @DeleteMapping("/deleteObject")
    public ResponseEntity<Void> deleteObject(@RequestParam String key) throws IOException {
        awsService.deleteS3Object(key);
        return ResponseEntity.noContent().build();
    }

    // GET /s3/moveObject - Mueve un objeto de S3 a una nueva ubicación
    @GetMapping("/moveObject")
    public ResponseEntity<Void> moveObject(@RequestParam String sourceKey, @RequestParam String destinationKey) {
        awsService.moveS3Object(sourceKey, destinationKey);
        return ResponseEntity.ok().build();
    }

    // POST /s3/uploadObject - Sube un archivo a S3 usando MultipartFile
    @PostMapping("/uploadObject")
    public ResponseEntity<String> uploadObject(@RequestParam String key, @RequestParam MultipartFile fileContent) throws IOException {
        String result = awsService.uploadS3Object(key, fileContent);
        return ResponseEntity.ok(result);
    }
    
}
