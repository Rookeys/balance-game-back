package com.games.balancegameback.infra.repository.media.impl;

import com.games.balancegameback.core.exception.ErrorCode;
import com.games.balancegameback.core.exception.impl.NotFoundException;
import com.games.balancegameback.domain.media.Images;
import com.games.balancegameback.domain.user.Users;
import com.games.balancegameback.infra.entity.ImagesEntity;
import com.games.balancegameback.infra.entity.UsersEntity;
import com.games.balancegameback.infra.repository.media.ImageJpaRepository;
import com.games.balancegameback.infra.repository.media.MediaJpaRepository;
import com.games.balancegameback.service.media.repository.ImageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class ImageRepositoryImpl implements ImageRepository {

    private final ImageJpaRepository imageRepository;
    private final MediaJpaRepository mediaRepository;

    @Override
    public Images save(Images images) {
        return imageRepository.save(ImagesEntity.from(images)).toModel();
    }

    @Override
    public void update(Images images) {
        ImagesEntity imagesEntity = imageRepository.findById(images.getId())
                .orElseThrow(() -> new NotFoundException("이미지를 찾을 수 없습니다.", ErrorCode.NOT_FOUND_EXCEPTION));

        imagesEntity.update(images);
    }

    @Override
    public List<Images> findByRoomId(Long roomId) {
        List<ImagesEntity> entityList = imageRepository.findByGamesId(roomId);
        return entityList.stream().map(ImagesEntity::toModel).toList();
    }

    @Override
    public Images findById(Long id) {
        return imageRepository.findById(id)
                .map(ImagesEntity::toModel)
                .orElseThrow(() -> new IllegalArgumentException("Image not found with id: " + id));
    }

    @Override
    public Images findByUsers(Users users) {
        return Optional.ofNullable(imageRepository.findByUsers(UsersEntity.from(users)))
                .map(ImagesEntity::toModel)
                .orElse(null);
    }

    @Override
    public void delete(Long id) {
        if (!imageRepository.existsById(id)) {
            throw new IllegalArgumentException("Image not found with id: " + id);
        }

        imageRepository.deleteById(id);
        mediaRepository.deleteById(id);
    }
}
