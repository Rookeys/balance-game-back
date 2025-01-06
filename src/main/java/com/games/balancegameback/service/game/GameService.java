package com.games.balancegameback.service.game;

import com.games.balancegameback.dto.game.GameListResponse;
import com.games.balancegameback.dto.game.GameRequest;
import com.games.balancegameback.dto.game.GameResponse;
import com.games.balancegameback.service.game.impl.GameRoomService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GameService {

    private final GameRoomService gameRoomService;

    // 게임 저장
    public void saveGame(GameRequest gameRequest, HttpServletRequest request) {
        gameRoomService.saveGame(gameRequest, request);
    }

    // 게임 설정값 반환
    public GameResponse getGameStatus(Long roomId, HttpServletRequest request) {
        return gameRoomService.getGameStatus(roomId, request);
    }

    // 내가 만든 게임들 리스트 반환
    public Page<GameListResponse> getMyGameList(Pageable pageable, Long cursorId, HttpServletRequest request) {
        return gameRoomService.getMyGameList(pageable, cursorId, request);
    }
}
