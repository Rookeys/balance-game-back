package com.games.balancegameback.service.media.repository;

import com.games.balancegameback.domain.media.Images;
import com.games.balancegameback.domain.user.Users;

import java.util.List;

public interface ImageRepository {

    Images save(Images images);

    List<Images> findByRoomId(Long roomId);

    Images findById(Long id);

    Images findByUsers(Users users);

    void delete(Long id);
}
