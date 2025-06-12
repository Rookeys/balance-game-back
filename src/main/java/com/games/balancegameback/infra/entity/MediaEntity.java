package com.games.balancegameback.infra.entity;

import com.games.balancegameback.domain.media.Media;
import com.games.balancegameback.domain.media.enums.MediaType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Inheritance(strategy = InheritanceType.JOINED)
@NoArgsConstructor
@Table(name = "media")
public abstract class MediaEntity extends BaseEntity {

    @Column(nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    protected MediaType mediaType;

    @Column(name = "user_id", length = 36)
    protected String userId;

    @Column(name = "game_id", length = 36)
    protected String gameId;

    public MediaEntity(String id, MediaType mediaType) {
        this.id = id;
        this.mediaType = mediaType;
    }

    @Override
    protected String getEntityPrefix() {
        return "MED";
    }

    @PrePersist
    public void prePersist() {
        generateId();
    }

    public abstract Media toModel();
}

