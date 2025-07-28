package com.games.balancegameback.service.media.impl;

import com.games.balancegameback.infra.repository.media.ImageJpaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserMediaCleanupService {

    private final ImageJpaRepository imageRepository;
    private final S3Service s3Service;

    /**
     * 사용자의 모든 미디어 파일 정리 (DB + S3)
     */
    @Transactional
    public void cleanupUserMediaFiles(String uid) {
        try {
            List<String> imageUrls = getImageUrlsByUser(uid);

            if (!imageUrls.isEmpty()) {
                log.info("사용자 {}의 이미지 {}개를 S3에서 삭제합니다.", uid, imageUrls.size());

                s3Service.deleteImagesAsync(imageUrls);
                imageRepository.deleteByUsersUid(uid);

                log.info("사용자 {}의 이미지 파일 정리 완료", uid);
            } else {
                log.debug("사용자 {}는 삭제할 이미지가 없습니다.", uid);
            }

        } catch (Exception e) {
            log.error("사용자 {}의 미디어 파일 정리 중 오류 발생: {}", uid, e.getMessage(), e);
        }
    }

    /**
     * 사용자가 업로드한 이미지 URL 목록 조회
     */
    private List<String> getImageUrlsByUser(String uid) {
        return imageRepository.findFileUrlsByUsersUid(uid);
    }

    /**
     * 특정 게임의 모든 미디어 파일 정리
     */
    @Transactional
    public void cleanupGameMediaFiles(Long gameId) {
        try {
            List<String> imageUrls = getImageUrlsByGame(gameId);

            if (!imageUrls.isEmpty()) {
                log.info("게임 {}의 이미지 {}개를 S3에서 삭제합니다.", gameId, imageUrls.size());

                s3Service.deleteImagesAsync(imageUrls);

                log.info("게임 {}의 이미지 파일 S3 삭제 요청 완료", gameId);
            }

        } catch (Exception e) {
            log.error("게임 {}의 미디어 파일 정리 중 오류 발생: {}", gameId, e.getMessage(), e);
        }
    }

    /**
     * 게임에 속한 이미지 URL 목록 조회
     */
    private List<String> getImageUrlsByGame(Long gameId) {
        return imageRepository.findFileUrlsByGamesId(gameId);
    }
}
