package com.games.balancegameback.infra.entity;

import com.games.balancegameback.domain.game.GameResources;
import jakarta.persistence.*;
import lombok.Getter;

@Getter
@Entity
@Table(name = "game_resources", indexes = {
        @Index(name = "idx_game_resources_game_id", columnList = "game_id")
})
public class GameResourcesEntity extends BaseEntity {

    @Column(length = 100)
    private String title;

    @Column(name = "game_id", nullable = false, length = 36)
    private String gameId;

    @Column(name = "image_id", length = 36)
    private String imageId;

    @Column(name = "link_id", length = 36)
    private String linkId;

    @Override
    protected String getEntityPrefix() {
        return "GRS";
    }

    @PrePersist
    public void prePersist() {
        generateId();
    }

    public static GameResourcesEntity from(GameResources gameResources) {
        GameResourcesEntity entity = new GameResourcesEntity();
        entity.id = gameResources.getId();
        entity.title = gameResources.getTitle() != null ? gameResources.getTitle() : "";
        entity.gameId = gameResources.getGames().getId();

        if (gameResources.getImages() != null) {
            entity.imageId = gameResources.getImages().getId();
        }

        if (gameResources.getLinks() != null) {
            entity.linkId = gameResources.getLinks().getId();
        }

        return entity;
    }

    public GameResources toModel() {
        return GameResources.builder()
                .id(id)
                .title(title != null ? title : "")
                .build();
    }

    public void update(GameResources gameResources) {
        this.title = gameResources.getTitle();
    }
}

