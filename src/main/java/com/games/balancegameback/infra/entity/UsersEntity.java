package com.games.balancegameback.infra.entity;

import com.games.balancegameback.domain.user.Users;
import com.games.balancegameback.domain.user.enums.LoginType;
import com.games.balancegameback.domain.user.enums.UserRole;
import jakarta.persistence.*;
import lombok.Getter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import java.time.LocalDateTime;

@Getter
@Entity
@Table(name = "users")
public class UsersEntity {

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

    @Column(updatable = false)
    @CreatedDate
    private LocalDateTime createdDate;

    @Column
    @LastModifiedDate
    private LocalDateTime updatedDate;

    public static UsersEntity from(Users user) {
        UsersEntity userEntity = new UsersEntity();
        userEntity.uid = user.uid();
        userEntity.nickname = user.nickname();
        userEntity.email = user.email();
        userEntity.loginType = user.loginType();
        userEntity.userRole = user.userRole();

        return userEntity;
    }

    public Users toModel() {
        return Users.builder()
                .uid(uid)
                .nickname(nickname)
                .email(email)
                .loginType(loginType)
                .userRole(userRole)
                .build();
    }
}

