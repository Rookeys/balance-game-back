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
public abstract class MediaEntity extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    protected MediaType mediaType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "users_id")
    protected UsersEntity users;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "games_id")
    protected GamesEntity games;

    public MediaEntity(Long id, MediaType mediaType) {
        this.id = id;
        this.mediaType = mediaType;
    }

    public abstract Media toModel();
}

