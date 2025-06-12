package com.games.balancegameback.infra.entity;

import com.games.balancegameback.domain.game.Games;
import com.games.balancegameback.domain.game.enums.AccessType;
import jakarta.persistence.*;
import lombok.Getter;

@Getter
@Entity
@Table(name = "games", indexes = {
        @Index(name = "idx_games_user_id", columnList = "user_id"),
        @Index(name = "idx_games_access_type", columnList = "access_type")
})
public class GamesEntity extends BaseEntity {

    @Column(nullable = false, length = 100)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    private Boolean isNamePrivate;

    @Column(nullable = false)
    private Boolean isBlind;

    @Column(nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private AccessType accessType;

    @Column(name = "user_id", nullable = false, length = 36)
    private String userId;

    @Override
    protected String getEntityPrefix() {
        return "GAM";
    }

    @PrePersist
    public void prePersist() {
        generateId();
    }

    public static GamesEntity from(Games games) {
        if (games == null) return null;

        GamesEntity entity = new GamesEntity();
        entity.id = games.getId();
        entity.title = games.getTitle();
        entity.description = games.getDescription();
        entity.isNamePrivate = games.getIsNamePrivate();
        entity.isBlind = games.getIsBlind();
        entity.accessType = games.getAccessType();
        entity.userId = games.getUsers().getUid();

        return entity;
    }

    public Games toModel() {
        return Games.builder()
                .id(id)
                .title(title)
                .description(description)
                .isNamePrivate(isNamePrivate)
                .isBlind(isBlind)
                .accessType(accessType)
                .build();
    }

    public void update(Games games) {
        this.title = games.getTitle();
        this.description = games.getDescription();
        this.isNamePrivate = games.getIsNamePrivate();
        this.isBlind = games.getIsBlind();
        this.accessType = games.getAccessType();
    }
}
