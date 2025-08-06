package com.games.balancegameback.infra.repository.game.common;

import com.games.balancegameback.infra.entity.*;

public final class GameQClasses {

    // 게임 관련
    public static final QGamesEntity games = QGamesEntity.gamesEntity;
    public static final QGameResourcesEntity resources = QGameResourcesEntity.gameResourcesEntity;
    public static final QGameCategoryEntity category = QGameCategoryEntity.gameCategoryEntity;
    public static final QGameResultsEntity results = QGameResultsEntity.gameResultsEntity;
    public static final QGamePlayEntity gamePlay = QGamePlayEntity.gamePlayEntity;

    // 사용자 관련
    public static final QUsersEntity users = QUsersEntity.usersEntity;

    // 미디어 관련
    public static final QImagesEntity images = QImagesEntity.imagesEntity;
    public static final QLinksEntity links = QLinksEntity.linksEntity;

    // 팔로우 관련
    //public static final QFollowEntity follow = QFollowEntity.followEntity;

    // 최근 플레이 관련
    //public static final QRecentPlayEntity recentPlay = QRecentPlayEntity.recentPlayEntity;

    private GameQClasses() {
        throw new AssertionError("Q-Classes holder class should not be instantiated");
    }
}
