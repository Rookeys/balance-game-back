package com.games.balancegameback.service.media.impl;

import com.games.balancegameback.domain.game.Games;
import com.games.balancegameback.dto.media.LinkRequest;
import com.games.balancegameback.service.game.GameService;
import com.games.balancegameback.service.game.repository.GameRepository;
import com.games.balancegameback.service.media.repository.LinkRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class LinkService {

    private final LinkRepository linkRepository;
    private final GameRepository gameRepository;
    private final GameService gameService;

    /**
     * 유튜브 링크 저장
     *
     * @param roomId     게임방 ID
     * @param linkRequest 링크 정보 DTO
     */
    public void saveLink(Long roomId, LinkRequest linkRequest) {
        Games games = gameRepository.findByRoomId(roomId);
        gameService.saveLinkResource(games, linkRequest);
    }

    public void deleteLink(Long id) {
        linkRepository.delete(id);
    }
}
