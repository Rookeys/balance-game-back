package com.games.balancegameback.service.media.impl;

import com.games.balancegameback.domain.game.Games;
import com.games.balancegameback.domain.media.Links;
import com.games.balancegameback.domain.user.Users;
import com.games.balancegameback.dto.media.LinkRequest;
import com.games.balancegameback.service.game.GameService;
import com.games.balancegameback.service.game.repository.GameRepository;
import com.games.balancegameback.service.media.repository.LinkRepository;
import com.games.balancegameback.service.user.impl.UserUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LinkService {

    private final LinkRepository linkRepository;
    private final GameRepository gameRepository;
    private final GameService gameService;
    private final UserUtils userUtils;

    /**
     * 유튜브 링크 저장
     *
     * @param roomId     게임방 ID
     * @param linkRequest 링크 정보 DTO
     */
    public void saveLink(Long roomId, LinkRequest linkRequest, HttpServletRequest request) {
        Users users = userUtils.findUserByToken(request);
        Games games = gameRepository.findByRoomId(roomId);
        Links links = Links.builder()
                .users(users)
                .games(games)
                .urls(linkRequest.getUrl())
                .startSec(linkRequest.getStartSec())
                .endSec(linkRequest.getEndSec())
                .build();

        links = linkRepository.save(links);
        gameService.saveLinkResource(games, links);
    }

    public void updateLink(Long id, LinkRequest linkRequest) {
        Links links = linkRepository.findById(id);
        links.update(linkRequest.getUrl(), linkRequest.getStartSec(), linkRequest.getEndSec());

        linkRepository.save(links);
    }

    public void deleteLink(Long id) {
        linkRepository.delete(id);
    }
}
