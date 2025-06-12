package com.games.balancegameback.domain.game;

import com.games.balancegameback.domain.game.enums.AccessType;
import com.games.balancegameback.domain.user.Users;
import com.games.balancegameback.dto.game.GameRequest;
import lombok.Builder;
import lombok.Data;

@Data
public class Games {

    private final String id;
    private String title;
    private String description;
    private Boolean isNamePrivate;
    private Boolean isBlind;
    private AccessType accessType;
    private final Users users;

    @Builder
    public Games(String id, String title, String description, Boolean isNamePrivate, Boolean isBlind,
                 AccessType accessType, Users users) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.isNamePrivate = isNamePrivate;
        this.isBlind = isBlind;
        this.accessType = accessType;
        this.users = users;
    }

    public void update(GameRequest gameRequest) {
        this.title = gameRequest.getTitle();
        this.description = gameRequest.getDescription();
        this.isNamePrivate = gameRequest.isExistsNamePrivate();
        this.isBlind = gameRequest.isExistsBlind();
        this.accessType = gameRequest.getAccessType();
    }
}
