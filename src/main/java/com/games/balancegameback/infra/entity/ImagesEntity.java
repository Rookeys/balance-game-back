package com.games.balancegameback.infra.entity;

import com.games.balancegameback.domain.media.Images;
import com.games.balancegameback.domain.media.enums.MediaType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "images")
@NoArgsConstructor
public class ImagesEntity extends MediaEntity {

    @Column(nullable = false)
    private String fileUrl;

    public ImagesEntity(Long id, String fileUrl, MediaType mediaType, GamesEntity games, UsersEntity users) {
        super(id, mediaType);
        this.fileUrl = fileUrl;
        this.games = games;
        this.users = users;
    }

    public static ImagesEntity from(Images images) {
        return new ImagesEntity(
                images.getId(),
                images.getFileUrl(),
                images.getMediaType(),
                images.getGames() != null ? GamesEntity.from(images.getGames()) : null,
                images.getUsers() != null ? UsersEntity.from(images.getUsers()) : null
        );
    }

    @Override
    public Images toModel() {
        return new Images(
                super.getId(),
                super.games != null ? super.games.toModel() : null,
                super.users != null ? super.users.toModel() : null,
                super.getMediaType(),
                this.fileUrl
        );
    }

    public void update(Images images) {
        this.fileUrl = images.getFileUrl();
    }
}

