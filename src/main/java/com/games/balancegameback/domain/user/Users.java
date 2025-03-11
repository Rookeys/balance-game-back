package com.games.balancegameback.domain.user;

import com.games.balancegameback.domain.user.enums.LoginType;
import com.games.balancegameback.domain.user.enums.UserRole;
import lombok.*;

import java.util.UUID;

@Data
public class Users {

    private String uid;
    private String nickname;
    private String email;
    private LoginType loginType;
    private UserRole userRole;
    private boolean isDeleted;

    @Builder
    public Users(String uid, String nickname, String email, LoginType loginType, UserRole userRole, boolean isDeleted) {
        this.uid = uid.isEmpty() ? String.valueOf(UUID.randomUUID()) : uid;
        this.nickname = nickname;
        this.email = email;
        this.loginType = loginType;
        this.userRole = userRole;
        this.isDeleted = isDeleted;
    }
}
