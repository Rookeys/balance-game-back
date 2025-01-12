package com.games.balancegameback.infra.entity;

import com.games.balancegameback.domain.media.Media;
import com.games.balancegameback.domain.media.enums.MediaType;
import jakarta.persistence.*;
import lombok.Getter;
import org.springframework.data.annotation.LastModifiedDate;

import java.time.LocalDateTime;

@Getter
@Entity
@Inheritance(strategy = InheritanceType.JOINED)
@Table(name = "media")
public abstract class MediaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    protected MediaType mediaType = MediaType.IMAGE;

    @Column
    @LastModifiedDate
    private LocalDateTime updatedDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "users_id")
    protected UsersEntity users;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "games_id")
    protected GamesEntity games;

    public abstract Media toModel();
}

