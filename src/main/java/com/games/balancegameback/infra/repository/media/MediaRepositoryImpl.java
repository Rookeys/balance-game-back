package com.games.balancegameback.infra.repository.media;

import com.games.balancegameback.service.media.repository.MediaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class MediaRepositoryImpl implements MediaRepository {

    private final MediaJpaRepository mediaRepository;

}
