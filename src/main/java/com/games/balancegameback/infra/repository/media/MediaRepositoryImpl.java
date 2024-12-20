package com.games.balancegameback.infra.repository.media;

import com.games.balancegameback.service.media.MediaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class MediaRepositoryImpl implements MediaRepository {

    private final MediaJpaRepository mediaRepository;

}
