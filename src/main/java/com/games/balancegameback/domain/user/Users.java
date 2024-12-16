package com.games.balancegameback.domain.user;

import com.games.balancegameback.domain.user.enums.LoginType;
import com.games.balancegameback.domain.user.enums.UserRole;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Users {

    private String uid;
    private String nickname;
    private String email;
    private LoginType loginType;
    private UserRole userRole;
    private boolean isDeleted;
}
