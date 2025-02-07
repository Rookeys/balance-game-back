package com.games.balancegameback.service.media.repository;

import com.games.balancegameback.domain.media.Images;
import com.games.balancegameback.domain.user.Users;

public interface ImageRepository {

    Images save(Images images);

    void update(Images images);

    Images findById(Long id);

    Images findByUsers(Users users);

    void delete(Long id);
}
