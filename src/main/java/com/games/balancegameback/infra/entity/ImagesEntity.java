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
    private String fileName;

    @Column(nullable = false)
    private String fileUrl;

    public static ImagesEntity from(Images images) {
        ImagesEntity imagesEntity = new ImagesEntity();
        imagesEntity.fileName = images.fileName();
        imagesEntity.fileUrl = images.fileUrl();

        return imagesEntity;
    }

    @Override
    public Images toModel() {
        return Images.builder()
                .id(this.getId())
                .fileName(fileName)
                .fileUrl(fileUrl)
                .build();
    }
}

