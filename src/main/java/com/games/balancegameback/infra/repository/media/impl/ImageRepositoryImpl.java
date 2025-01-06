package com.games.balancegameback.infra.repository.media.impl;

import com.games.balancegameback.infra.repository.media.ImageJpaRepository;
import com.games.balancegameback.service.media.repository.ImageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class ImageRepositoryImpl implements ImageRepository {

    private final ImageJpaRepository imageRepository;


}
