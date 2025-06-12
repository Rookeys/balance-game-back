package com.games.balancegameback.service.game.impl;

import com.games.balancegameback.core.exception.ErrorCode;
import com.games.balancegameback.core.exception.impl.UnAuthorizedException;
import com.games.balancegameback.core.utils.CustomPageImpl;
import com.games.balancegameback.domain.game.Games;
import com.games.balancegameback.domain.user.Users;
import com.games.balancegameback.dto.game.*;
import com.games.balancegameback.infra.repository.game.*;
import com.games.balancegameback.service.game.repository.GameRepository;
import com.games.balancegameback.service.user.impl.UserUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Pageable;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class GameRoomService {

    private final GameRepository gameRepository;
    private final GameResultCommentJpaRepository gameResultCommentsRepository;
    private final GameResourceJpaRepository gameResourcesRepository;
    private final GamePlayJpaRepository gamePlayRepository;
    private final GameCategoryJpaRepository gameCategoryRepository;
    private final GameJpaRepository gameJpaRepository;

    private final GameCategoryService gameCategoryService;
    private final UserUtils userUtils;
    private final RestTemplate restTemplate;

    @Value("${front.secret}")
    private String secret;

    @Transactional
    public String saveGame(GameRequest gameRequest, HttpServletRequest request) {
        Users users = userUtils.findUserByToken(request);

        // 게임방 생성
        Games games = Games.builder()
                .title(gameRequest.getTitle())
                .description(gameRequest.getDescription())
                .accessType(gameRequest.getAccessType())
                .isNamePrivate(gameRequest.isExistsNamePrivate())
                .isBlind(gameRequest.isExistsBlind())
                .users(users)
                .build();

        games = gameRepository.save(games);
        gameCategoryService.saveCategory(gameRequest.getCategories(), games);

        this.revalidate("/game/" + games.getId());

        return games.getId();
    }

    public GameResponse getMyGameStatus(Long gameId, HttpServletRequest request) {
        Users users = userUtils.findUserByToken(request);
        this.existsHost(gameId, users);

        return gameRepository.findById(gameId);
    }

    public CustomPageImpl<GameListResponse> getMyGameList(Pageable pageable, Long cursorId,
                                                          GameSearchRequest searchRequest,
                                                          HttpServletRequest request) {
        Users users = userUtils.findUserByToken(request);
        return gameRepository.findGamesWithResources(cursorId, users, pageable, searchRequest);
    }

    @Transactional
    public void updateGameStatus(Long gameId, GameRequest gameRequest, HttpServletRequest request) {
        Users users = userUtils.findUserByToken(request);
        this.existsHost(gameId, users);

        Games games = gameRepository.findByRoomId(gameId);
        games.update(gameRequest);

        gameCategoryService.updateCategory(gameRequest.getCategories(), games);

        gameRepository.update(games);

        this.revalidate("/game/" + gameId);
    }

    @Transactional
    public void deleteGame(Long gameId, HttpServletRequest request) {
        Users users = userUtils.findUserByToken(request);
        this.existsHost(gameId, users);

        // 해당 게임과 관련된 데이터 일괄 삭제
        this.deleteGameData(gameId);

        this.revalidate("/game/" + gameId);
    }

    private void existsHost(Long gameId, Users users) {
        if (!gameRepository.existsIdAndUsers(gameId, users)) {
            throw new UnAuthorizedException("게임 주인이 아닙니다.", ErrorCode.NOT_ALLOW_WRITE_EXCEPTION);
        }
    }

    private void revalidate(String path) {
        // JSON payload 구성
        Map<String, String> body = new HashMap<>();
        body.put("path", path);
        body.put("secret", secret);

        // HTTP 헤더 설정
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        // HttpEntity 생성
        HttpEntity<Map<String, String>> request = new HttpEntity<>(body, headers);

        // POST 요청 전송
        String url = "https://zznpk.com/api/revalidate";
        ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);

        log.info(response.getBody());
    }

    private void deleteGameData(Long gameId) {
        gameCategoryRepository.deleteByGamesId(gameId);
        gamePlayRepository.deleteByGamesId(gameId);
        gameResourcesRepository.deleteByGamesId(gameId);
        gameResultCommentsRepository.deleteByGamesId(gameId);

        gameJpaRepository.deleteById(gameId);
    }
}
