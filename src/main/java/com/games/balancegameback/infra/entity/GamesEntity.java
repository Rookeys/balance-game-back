package com.games.balancegameback.infra.entity;

import com.games.balancegameback.domain.game.GameInviteCode;
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

    @OneToOne(mappedBy = "games", cascade = CascadeType.ALL, orphanRemoval = true)
    private GameInviteCodeEntity gameInviteCode;

    public static GamesEntity from(Games games) {
        if (games == null) return null;

        GamesEntity gamesEntity = new GamesEntity();
        gamesEntity.id = games.getId() == null ? null : games.getId();
        gamesEntity.title = games.getTitle();
        gamesEntity.description = games.getDescription();
        gamesEntity.isNamePublic = games.getIsNamePublic();
        gamesEntity.accessType = games.getAccessType();
        gamesEntity.category = games.getCategory();
        gamesEntity.users = UsersEntity.from(games.getUsers());

        // GameInviteCodeEntity 설정
        if (games.getGameInviteCode() != null) {
            GameInviteCodeEntity inviteCodeEntity = GameInviteCodeEntity.from(games.getGameInviteCode());
            gamesEntity.gameInviteCode = inviteCodeEntity;

            // 순환 참조 방지: gamesEntity만 설정
            inviteCodeEntity.setGames(gamesEntity);
        }

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
                .gameInviteCode(this.gameInviteCode != null
                        ? GameInviteCode.builder()
                            .id(this.gameInviteCode.getId())
                            .inviteCode(this.gameInviteCode.getInviteCode())
                            .isActive(this.gameInviteCode.getIsActive())
                            .build()
                        : null)
                .build();
    }

    public void update(Games games) {
        this.title = games.getTitle();
        this.description = games.getDescription();
        this.isNamePublic = games.getIsNamePublic();
        this.accessType = games.getAccessType();
        this.category = games.getCategory();
    }
}

