package com.games.balancegameback.domain.user;

import com.games.balancegameback.domain.user.enums.LoginType;
import com.games.balancegameback.domain.user.enums.UserRole;
import lombok.Builder;

public record Users(String uid, String nickname, String email, LoginType loginType, UserRole userRole) {

    @Builder
    public Users {

    }
}
