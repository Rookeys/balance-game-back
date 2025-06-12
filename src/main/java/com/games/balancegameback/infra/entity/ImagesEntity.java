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

    @Column(nullable = false, length = 500)
    private String fileUrl;

    public ImagesEntity(String id, String fileUrl, MediaType mediaType, String gameId, String userId) {
        super(id, mediaType);
        this.fileUrl = fileUrl;
        this.gameId = gameId;
        this.userId = userId;
    }

    @Override
    protected String getEntityPrefix() {
        return "IMG";
    }

    public static ImagesEntity from(Images images) {
        return new ImagesEntity(
                images.getId(),
                images.getFileUrl(),
                images.getMediaType(),
                images.getGames() != null ? images.getGames().getId() : null,
                images.getUsers() != null ? images.getUsers().getUid() : null
        );
    }

    @Override
    public Images toModel() {
        return Images.builder()
                .id(this.id)
                .fileUrl(this.fileUrl)
                .mediaType(this.mediaType)
                .build();
    }

    public void update(Images images) {
        this.fileUrl = images.getFileUrl();
    }
}

