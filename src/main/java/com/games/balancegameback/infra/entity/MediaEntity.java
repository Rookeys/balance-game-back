package com.games.balancegameback.infra.entity;

import com.games.balancegameback.domain.media.Media;
import com.games.balancegameback.domain.media.enums.MediaType;
import com.games.balancegameback.domain.media.enums.UsingType;
import jakarta.persistence.*;
import lombok.Getter;
import org.springframework.data.annotation.CreatedDate;

import java.time.LocalDateTime;

@Getter
@Entity
@Table(name = "media")
public class MediaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private MediaType mediaType;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private UsingType usingType;

    @Column(updatable = false)
    @CreatedDate
    private LocalDateTime createdDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "users_id")
    private UsersEntity users;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "games_id")
    private GamesEntity games;

    public static MediaEntity from(Media media) {
        MediaEntity mediaEntity = new MediaEntity();
        mediaEntity.mediaType = media.mediaType();
        mediaEntity.usingType = media.usingType();
        mediaEntity.users = UsersEntity.from(media.user());
        mediaEntity.games = GamesEntity.from(media.game());

        return mediaEntity;
    }

    public Media toModel() {
        return Media.builder()
                .id(id)
                .mediaType(mediaType)
                .usingType(usingType)
                .user(users.toModel())
                .game(games.toModel())
                .build();
    }
}

