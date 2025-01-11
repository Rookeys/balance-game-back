package com.games.balancegameback.infra.repository.media.impl;

import com.games.balancegameback.domain.media.Links;
import com.games.balancegameback.infra.entity.LinksEntity;
import com.games.balancegameback.infra.repository.media.LinkJpaRepository;
import com.games.balancegameback.service.media.repository.LinkRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class LinkRepositoryImpl implements LinkRepository {

    private final LinkJpaRepository linkRepository;

    @Override
    public Links save(Links links) {
        LinksEntity entity = linkRepository.save(LinksEntity.from(links));
        return entity.toModel();
    }

    @Override
    public Links findById(Long id) {
        Optional<LinksEntity> entity = linkRepository.findById(id);
        return entity.map(LinksEntity::toModel).orElse(null);
    }

    @Override
    public void delete(Long id) {
        linkRepository.deleteById(id);
    }
}
