package com.games.balancegameback.service.media.repository;

import com.games.balancegameback.domain.media.Images;

public interface ImageRepository {

    Images save(Images images);

    Images findById(Long id);

    void delete(Long id);
}
