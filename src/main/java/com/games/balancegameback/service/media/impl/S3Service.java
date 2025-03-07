package com.games.balancegameback.service.media.impl;

import com.games.balancegameback.core.exception.ErrorCode;
import com.games.balancegameback.core.exception.impl.CustomJwtException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;

import java.net.MalformedURLException;
import java.net.URI;
import java.util.List;

@Service
@RequiredArgsConstructor
public class S3Service {

    private final S3Client s3Client;

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    /**
     * 이미지 URL에서 S3 key를 추출한 후, 해당 객체를 삭제함.
     *
     * @param imageUrl S3에 저장된 이미지의 URL
     */
    public void deleteImageByUrl(String imageUrl) {
        try {
            var url = URI.create(imageUrl).toURL();
            var key = url.getPath();

            if (key.startsWith("/image/")) {
                key = key.substring(1);
            }

            var deleteRequest = DeleteObjectRequest.builder()
                    .bucket(bucket)
                    .key(key)
                    .build();

            s3Client.deleteObject(deleteRequest);
        } catch (MalformedURLException e) {
            throw new CustomJwtException(ErrorCode.INVALID_IMAGE_EXCEPTION, "URL 형식이 올바르지 않습니다.");
        }
    }

    /**
     * 여러 이미지 URL에 대해 비동기 삭제 작업을 실행함.
     *
     * @param imageUrls 삭제할 이미지 URL 목록
     */
    @Async
    public void deleteImagesAsync(List<String> imageUrls) {
        imageUrls.forEach(this::deleteImageByUrl);
    }
}
