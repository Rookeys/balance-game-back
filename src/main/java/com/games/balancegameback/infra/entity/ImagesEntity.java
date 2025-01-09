package com.games.balancegameback.infra.entity;

import com.games.balancegameback.domain.media.Images;
import jakarta.persistence.*;
import lombok.Getter;

@Getter
@Entity
@DiscriminatorValue("IMAGE")
@Table(name = "images")
public class ImagesEntity extends MediaEntity {

    @Column(nullable = false)
    private String fileUrl;

    public static ImagesEntity from(Images images) {
        ImagesEntity imagesEntity = new ImagesEntity();
        imagesEntity.fileUrl = images.getFileUrl();

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
                .fileUrl(fileUrl)
                .build();
    }
}

