package com.games.balancegameback.infra.repository.media.impl;

import com.games.balancegameback.infra.repository.media.MediaJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class MediaRepositoryImpl {

    private final MediaJpaRepository mediaRepository;


}
