package com.hc.application.service;

import java.io.IOException;
import java.net.URI;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import jakarta.annotation.PreDestroy;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Configuration;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

@Service
public class GarageStorageService {

    private final S3Client s3Client;
    private final String bucket;

    public GarageStorageService(
            @Value("${GARAGE_S3_ENDPOINT}") String endpoint,
            @Value("${GARAGE_S3_REGION}") String region,
            @Value("${GARAGE_S3_BUCKET}") String bucket,
            @Value("${GARAGE_S3_ACCESS_KEY_ID}") String accessKey,
            @Value("${GARAGE_S3_SECRET_KEY}") String secretKey) {

        this.bucket = bucket;
        this.s3Client = S3Client.builder()
                .endpointOverride(URI.create(endpoint))
                .region(Region.of(region))
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(accessKey, secretKey)))
                .serviceConfiguration(S3Configuration.builder()
                        .pathStyleAccessEnabled(true)
                        .build())
                .build();
    }

    public void subir(String key, MultipartFile archivo) throws IOException {
        String contentType = archivo.getContentType();

        if (contentType == null || archivo.isEmpty()) {
            throw new IllegalArgumentException("El archivo está vacío o no tiene tipo.");
        }

        PutObjectRequest request = PutObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .contentType(contentType)
                .build();

        s3Client.putObject(
                request,
                RequestBody.fromInputStream(archivo.getInputStream(), archivo.getSize()));
    }

    public byte[] descargar(String key) throws IOException {
        GetObjectRequest request = GetObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .build();

        try (ResponseInputStream<GetObjectResponse> stream =
                     s3Client.getObject(request)) {
            return stream.readAllBytes();
        }
    }

    public void eliminar(String key) {
        s3Client.deleteObject(DeleteObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .build());
    }

    @PreDestroy
    public void cerrar() {
        s3Client.close();
    }
}