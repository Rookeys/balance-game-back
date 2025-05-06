package com.games.balancegameback.domain.game;

import com.games.balancegameback.domain.game.enums.AccessType;
import com.games.balancegameback.domain.user.Users;
import com.games.balancegameback.dto.game.GameRequest;
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
    private final Users users;
    private GameInviteCode gameInviteCode;
    private List<GameResources> gameResources;
    private List<GameCategory> categories;

    @Builder
    public Games(Long id, String title, String description, Boolean isNamePrivate, Boolean isBlind,
                 AccessType accessType, Users users, GameInviteCode gameInviteCode,
                 List<GameResources> gameResources, List<GameCategory> categories) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.isNamePrivate = isNamePrivate;
        this.isBlind = isBlind;
        this.accessType = accessType;
        this.users = users;
        this.gameInviteCode = gameInviteCode;
        this.gameResources = gameResources;
        this.categories = categories;
    }

    public void update(GameRequest gameRequest) {
        this.title = gameRequest.getTitle();
        this.description = gameRequest.getDescription();
        this.isNamePrivate = gameRequest.isExistsNamePrivate();
        this.isBlind = gameRequest.isExistsBlind();
        this.accessType = gameRequest.getAccessType();
    }
}
