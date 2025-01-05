package com.games.balancegameback.service.media.impl;

import com.games.balancegameback.dto.media.LinkRequest;
import com.games.balancegameback.service.media.repository.LinkRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LinkService {

//    private final LinkRepository linkRepository;
//    private final GamesRepository gamesRepository;

    /**
     * 유튜브 링크 저장
     *
     * @param roomId     게임방 ID
     * @param linkRequest 링크 정보 DTO
     */
    public void saveLink(Long roomId, LinkRequest linkRequest) {

    }
}
