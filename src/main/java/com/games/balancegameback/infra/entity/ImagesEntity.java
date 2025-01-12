package com.games.balancegameback.infra.entity;

import com.games.balancegameback.domain.media.Images;
import com.games.balancegameback.domain.media.enums.MediaType;
import jakarta.persistence.*;
import lombok.Getter;

@Getter
@Entity
@Table(name = "images")
public class ImagesEntity extends MediaEntity {

    @Column(nullable = false)
    private String fileUrl;

    public static ImagesEntity from(Images images) {
        ImagesEntity imagesEntity = new ImagesEntity();
        imagesEntity.fileUrl = images.getFileUrl();
        imagesEntity.mediaType = images.getMediaType() == null ? MediaType.IMAGE : images.getMediaType();

        if (images.getGames() != null) {
            imagesEntity.games = GamesEntity.from(images.getGames());
        }

        if (images.getUsers() != null) {
            imagesEntity.users = UsersEntity.from(images.getUsers());
        }

        return imagesEntity;
    }

    @Override
    public Images toModel() {
        return Images.builder()
                .id(this.getId())
                .users(users == null ? null : users.toModel())
                .games(games == null ? null : games.toModel())
                .mediaType(MediaType.IMAGE)
                .fileUrl(fileUrl)
                .build();
    }
}

