package com.games.balancegameback.infra.entity;

import com.games.balancegameback.domain.user.Users;
import com.games.balancegameback.domain.user.enums.LoginType;
import com.games.balancegameback.domain.user.enums.UserRole;
import jakarta.persistence.*;
import lombok.Getter;

import java.util.UUID;

@Getter
@Entity
@Table(name = "users")
public class UsersEntity extends BaseTimeEntity {

    @Id
    private String uid;

    @Column(unique = true, nullable = false)
    private String nickname;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private LoginType loginType;

    @Column
    @Enumerated(EnumType.STRING)
    private UserRole userRole;

    @Column
    private Boolean isDeleted = false;

    public static UsersEntity from(Users user) {
        UsersEntity userEntity = new UsersEntity();
        userEntity.uid = user.getUid();
        userEntity.nickname = user.getNickname();
        userEntity.email = user.getEmail();
        userEntity.loginType = user.getLoginType();
        userEntity.userRole = user.getUserRole();
        userEntity.isDeleted = user.isDeleted();

        return userEntity;
    }

    public Users toModel() {
        return Users.builder()
                .uid(uid)
                .nickname(nickname)
                .email(email)
                .loginType(loginType)
                .userRole(userRole)
                .isDeleted(isDeleted)
                .build();
    }

    public void update(Users user) {
        this.nickname = user.getNickname();
        this.isDeleted = user.isDeleted();
    }
}

