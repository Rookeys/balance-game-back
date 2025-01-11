package com.games.balancegameback.domain.game;

import com.games.balancegameback.domain.game.enums.AccessType;
import com.games.balancegameback.domain.game.enums.Category;
import com.games.balancegameback.domain.user.Users;
import lombok.Builder;

public record Games(Long id, String title, String description, Boolean isNamePublic, AccessType accessType,
                    Category category, Users users, GameInviteCode gameInviteCode) {

    @Builder
    public Games {

    }
}
