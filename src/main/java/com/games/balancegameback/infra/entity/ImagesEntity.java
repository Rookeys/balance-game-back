package com.games.balancegameback.infra.entity;

import com.games.balancegameback.domain.media.Images;
import com.games.balancegameback.domain.media.Links;
import jakarta.persistence.*;
import lombok.Getter;
import org.springframework.data.annotation.CreatedDate;

import java.time.LocalDateTime;

@Getter
@Entity
@Table(name = "images")
public class ImagesEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String fileName;

    @Column(nullable = false)
    private String fileUrl;

    @Column(updatable = false)
    @CreatedDate
    private LocalDateTime createdDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "media_id")
    private MediaEntity media;

    public static ImagesEntity from(Images images) {
        ImagesEntity imagesEntity = new ImagesEntity();
        imagesEntity.fileName = images.fileName();
        imagesEntity.fileUrl = images.fileUrl();
        imagesEntity.media = MediaEntity.from(images.media());

        return imagesEntity;
    }

    public Images toModel() {
        return Images.builder()
                .id(id)
                .fileName(fileName)
                .fileUrl(fileUrl)
                .media(media.toModel())
                .build();
    }
}

