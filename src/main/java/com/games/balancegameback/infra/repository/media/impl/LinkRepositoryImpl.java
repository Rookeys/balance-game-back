package com.games.balancegameback.infra.repository.media.impl;

import com.games.balancegameback.core.exception.ErrorCode;
import com.games.balancegameback.core.exception.impl.NotFoundException;
import com.games.balancegameback.domain.media.Links;
import com.games.balancegameback.infra.entity.LinksEntity;
import com.games.balancegameback.infra.repository.media.LinkJpaRepository;
import com.games.balancegameback.infra.repository.media.MediaJpaRepository;
import com.games.balancegameback.service.media.repository.LinkRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class LinkRepositoryImpl implements LinkRepository {

    private final LinkJpaRepository linkRepository;
    private final MediaJpaRepository mediaRepository;

    @Override
    public Links save(Links links) {
        LinksEntity entity = linkRepository.save(LinksEntity.from(links));
        return entity.toModel();
    }

    @Override
    public void update(Links links) {
        LinksEntity link = linkRepository.findById(links.getId())
                .orElseThrow(() -> new NotFoundException("이미지를 찾을 수 없습니다.", ErrorCode.NOT_FOUND_EXCEPTION));

        link.update(links);
    }

    @Override
    public Links findById(Long id) {
        Optional<LinksEntity> entity = linkRepository.findById(id);
        return entity.map(LinksEntity::toModel).orElse(null);
    }

    @Override
    public void delete(Long id) {
        linkRepository.deleteById(id);
        mediaRepository.deleteById(id);
    }
}
