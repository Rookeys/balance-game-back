package com.games.balancegameback.service.media.repository;

import com.games.balancegameback.domain.media.Links;

public interface LinkRepository {

    Links save(Links links);

    Links findById(Long id);

    void delete(Long id);
}
