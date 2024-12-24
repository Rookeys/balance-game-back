package com.games.balancegameback.service.media.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import java.net.URL;
import java.time.Duration;
import java.util.*;

@Service
@RequiredArgsConstructor
public class ImageService {

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    private final S3Presigner s3Presigner;
    private static final Region REGION = Region.AP_NORTHEAST_2;

    /**
     * Presigned URL 발급
     * @param prefix 버킷 디렉토리 이름
     * @param fileName 클라이언트가 전달한 파일명 파라미터
     * @return Presigned URL
     */
    public Map<String, String> getPreSignedUrl(String prefix, String fileName) {
        if (prefix != null && !prefix.isEmpty()) {
            fileName = createPath(prefix, fileName);
        }

        // Presigned URL 생성
        PresignedPutObjectRequest presignedRequest = generatePreSignedUrlRequest(bucket, fileName);
        URL url = presignedRequest.url();

        // 최종 S3 URL 생성
        String finalUrl = String.format("https://%s.s3.%s.amazonaws.com/%s", bucket, REGION, fileName);

        Map<String, String> result = new HashMap<>();
        result.put("presignedUrl", url.toString());
        result.put("finalUrl", finalUrl);

        return result;
    }

    /**
     * 여러 장의 Presigned URL 발급 및 S3 URL 반환
     * @param prefix 버킷 디렉토리 이름
     * @param fileNames 클라이언트가 전달한 파일명 리스트
     * @return List<Map<String, String>> (presignedUrl, finalUrl)
     */
    public List<Map<String, String>> getPreSignedUrls(String prefix, List<String> fileNames) {
        List<Map<String, String>> urls = new ArrayList<>();

        for (String fileName : fileNames) {
            Map<String, String> url = getPreSignedUrl(prefix, fileName);
            urls.add(url);
        }

        return urls;
    }

    /**
     * 파일 업로드용(PUT) presigned url 생성
     * @param bucket 버킷 이름
     * @param fileName S3 업로드용 파일 이름
     * @return Presigned URL
     */
    private PresignedPutObjectRequest generatePreSignedUrlRequest(String bucket, String fileName) {
        // PutObjectRequest 설정
        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucket)
                .key(fileName)
                .build();

        // Presign 요청 설정
        PutObjectPresignRequest presignRequest = PutObjectPresignRequest.builder()
                .putObjectRequest(putObjectRequest)
                .signatureDuration(Duration.ofMinutes(2)) // 유효 시간 2분
                .build();

        // Presigned URL 생성
        return s3Presigner.presignPutObject(presignRequest);
    }

    /**
     * 파일 고유 ID를 생성
     * @return 36자리의 UUID
     */
    private String createFileId() {
        return UUID.randomUUID().toString();
    }

    /**
     * 파일의 전체 경로를 생성
     * @param prefix 디렉토리 경로
     * @param fileName 파일 이름
     * @return 파일의 전체 경로
     */
    private String createPath(String prefix, String fileName) {
        String fileId = createFileId();
        return String.format("%s/%s%s", prefix, fileId, fileName);
    }
}
