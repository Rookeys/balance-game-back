package com.games.balancegameback.infra.repository.media.impl;

import com.games.balancegameback.domain.media.Images;
import com.games.balancegameback.infra.entity.ImagesEntity;
import com.games.balancegameback.infra.repository.media.ImageJpaRepository;
import com.games.balancegameback.service.media.repository.ImageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class ImageRepositoryImpl implements ImageRepository {

    private final ImageJpaRepository imageRepository;

    @Override
    public Images save(Images images) {
        ImagesEntity entity = imageRepository.save(ImagesEntity.from(images));
        return entity.toModel();
    }

    @Override
    public Images findById(Long id) {
        Optional<ImagesEntity> entity = imageRepository.findById(id);
        return entity.map(ImagesEntity::toModel).orElse(null);
    }

    @Override
    public void delete(Long id) {
        imageRepository.deleteById(id);
    }
}
