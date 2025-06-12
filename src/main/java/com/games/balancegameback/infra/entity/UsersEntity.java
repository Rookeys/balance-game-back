package com.games.balancegameback.infra.entity;

import com.games.balancegameback.domain.user.Users;
import com.games.balancegameback.domain.user.enums.LoginType;
import com.games.balancegameback.domain.user.enums.UserRole;
import jakarta.persistence.*;
import lombok.Getter;

@Getter
@Entity
@Table(name = "users", indexes = {
        @Index(name = "idx_users_email", columnList = "email"),
        @Index(name = "idx_users_nickname", columnList = "nickname")
})
public class UsersEntity extends BaseEntity {

    @Column(unique = true, nullable = false, length = 100)
    private String email;

    @Column(unique = true, nullable = false, length = 50)
    private String nickname;

    @Column(nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private LoginType loginType;

    @Column(nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private UserRole userRole;

    @Column(nullable = false)
    private Boolean isDeleted = false;

    @Override
    protected String getEntityPrefix() {
        return "USR";
    }

    @PrePersist
    public void prePersist() {
        generateId();
    }

    public static UsersEntity from(Users user) {
        UsersEntity entity = new UsersEntity();
        entity.id = user.getUid();
        entity.email = user.getEmail();
        entity.nickname = user.getNickname();
        entity.loginType = user.getLoginType();
        entity.userRole = user.getUserRole();
        entity.isDeleted = user.isDeleted();
        return entity;
    }

    public Users toModel() {
        return Users.builder()
                .uid(id)
                .email(email)
                .nickname(nickname)
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

