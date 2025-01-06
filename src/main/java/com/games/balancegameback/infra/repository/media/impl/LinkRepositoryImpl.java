package com.games.balancegameback.infra.repository.media.impl;

import com.games.balancegameback.infra.repository.media.LinkJpaRepository;
import com.games.balancegameback.service.media.repository.LinkRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class LinkRepositoryImpl implements LinkRepository {

    private final LinkJpaRepository linkRepository;


}
