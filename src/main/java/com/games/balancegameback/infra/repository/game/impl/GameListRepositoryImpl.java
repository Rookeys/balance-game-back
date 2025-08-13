package com.games.balancegameback.infra.repository.game.impl;

import com.games.balancegameback.core.utils.CustomPageImpl;
import com.games.balancegameback.domain.game.enums.GameListType;
import com.games.balancegameback.domain.user.Users;
import com.games.balancegameback.dto.game.*;
import com.games.balancegameback.infra.repository.game.common.AbstractGameRepository;
import com.games.balancegameback.infra.repository.game.common.CommonGameRepository;
import com.games.balancegameback.service.game.repository.GameListRepository;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

@Repository
public class GameListRepositoryImpl extends AbstractGameRepository implements GameListRepository {

    public GameListRepositoryImpl(JPAQueryFactory jpaQueryFactory, CommonGameRepository commonGameRepository) {
        super(jpaQueryFactory, commonGameRepository);
    }

    @Override
    public GameCategoryNumsResponse getCategoryCounts(String title) {
        return getCategoryCountsAutomated(title, GameListType.PUBLIC, null);
    }

    @Override
    public GameDetailResponse getGameStatus(Long gameId, Users user) {
        return getGameDetailAutomated(gameId, user);
    }

    @Override
    public CustomPageImpl<GameListResponse> getGameList(Long cursorId, Pageable pageable,
                                                        GameSearchRequest searchRequest, Users users) {
        return findGameListAutomated(cursorId, pageable, searchRequest, users, GameListType.PUBLIC);
    }
}