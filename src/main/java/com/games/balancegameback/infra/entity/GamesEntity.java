package com.games.balancegameback.infra.entity;

import com.games.balancegameback.domain.game.Games;
import com.games.balancegameback.domain.game.enums.AccessType;
import com.games.balancegameback.domain.game.enums.Category;
import jakarta.persistence.*;
import lombok.Getter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import java.time.LocalDateTime;

@Getter
@Entity
@Table(name = "games")
public class GamesEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    private Boolean isNamePublic;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private AccessType accessType;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Category category;

    @Column(updatable = false)
    @CreatedDate
    private LocalDateTime createdDate;

    @Column
    @LastModifiedDate
    private LocalDateTime updatedDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "users_id")
    private UsersEntity users;

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "game_invite_code_id")
    private GameInviteCodeEntity gameInviteCode;

    public static GamesEntity from(Games games) {
        GamesEntity gamesEntity = new GamesEntity();
        gamesEntity.id = games.id();
        gamesEntity.title = games.title();
        gamesEntity.description = games.description();
        gamesEntity.isNamePublic = games.isNamePublic();
        gamesEntity.accessType = games.accessType();
        gamesEntity.category = games.category();
        gamesEntity.users = UsersEntity.from(games.users());
        gamesEntity.gameInviteCode = GameInviteCodeEntity.from(games.gameInviteCode());

        return gamesEntity;
    }

    public Games toModel() {
        return Games.builder()
                .id(id)
                .title(title)
                .description(description)
                .isNamePublic(isNamePublic)
                .accessType(accessType)
                .category(category)
                .users(users.toModel())
                .gameInviteCode(gameInviteCode.toModel())
                .build();
    }
}

