package com.games.balancegameback.domain.game;

import com.games.balancegameback.domain.game.enums.AccessType;
import com.games.balancegameback.domain.game.enums.Category;
import com.games.balancegameback.domain.user.Users;
import com.games.balancegameback.dto.game.GameRequest;
import lombok.Builder;
import lombok.Data;

@Data
public class Games {

    private final Long id;
    private String title;
    private String description;
    private Boolean isNamePublic;
    private AccessType accessType;
    private Category category;
    private final Users users;
    private GameInviteCode gameInviteCode;

    @Builder
    public Games(Long id, String title, String description, Boolean isNamePublic, AccessType accessType,
                 Category category, Users users, GameInviteCode gameInviteCode) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.isNamePublic = isNamePublic;
        this.accessType = accessType;
        this.category = category;
        this.users = users;
        this.gameInviteCode = gameInviteCode;
    }

    public void update(GameRequest gameRequest) {
        this.title = gameRequest.getTitle();
        this.description = gameRequest.getDescription();
        this.isNamePublic = gameRequest.isNamePublic();
        this.accessType = gameRequest.getAccessType();
        this.category = gameRequest.getCategory();
    }
}
