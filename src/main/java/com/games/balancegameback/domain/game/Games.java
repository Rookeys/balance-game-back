package com.games.balancegameback.domain.game;

import com.games.balancegameback.domain.game.enums.AccessType;
import com.games.balancegameback.domain.game.enums.Category;
import com.games.balancegameback.domain.user.Users;
import com.games.balancegameback.dto.game.GameRequest;
import com.games.balancegameback.infra.entity.GameResourcesEntity;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
public class Games {

    private final Long id;
    private String title;
    private String description;
    private Boolean isNamePrivate;
    private Boolean isBlind;
    private AccessType accessType;
    private Category category;
    private final Users users;
    private GameInviteCode gameInviteCode;
    private List<GameResources> gameResources;

    @Builder
    public Games(Long id, String title, String description, Boolean isNamePrivate, Boolean isBlind,
                 AccessType accessType, Category category, Users users, GameInviteCode gameInviteCode,
                 List<GameResources> gameResources) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.isNamePrivate = isNamePrivate;
        this.isBlind = isBlind;
        this.accessType = accessType;
        this.category = category;
        this.users = users;
        this.gameInviteCode = gameInviteCode;
        this.gameResources = gameResources;
    }

    public void update(GameRequest gameRequest) {
        this.title = gameRequest.getTitle();
        this.description = gameRequest.getDescription();
        this.isNamePrivate = gameRequest.isNamePrivate();
        this.isBlind = gameRequest.isBlind();
        this.accessType = gameRequest.getAccessType();
        this.category = gameRequest.getCategory();
    }
}
