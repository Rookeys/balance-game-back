package com.games.balancegameback.infra.entity;

import com.games.balancegameback.domain.media.Links;
import com.games.balancegameback.domain.media.enums.MediaType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import java.time.LocalDateTime;

@Getter
@Entity
@Table(name = "links")
@NoArgsConstructor
public class LinksEntity extends MediaEntity {

    @Column(nullable = false)
    private String urls;

    @Column
    private int startSec;

    @Column
    private int endSec;

    @Column(updatable = false)
    @CreatedDate
    private LocalDateTime createdDate;

    @Column
    @LastModifiedDate
    private LocalDateTime updatedDate;

    public LinksEntity(Long id, String urls, int startSec, int endSec, MediaType mediaType) {
        super(id, mediaType == null ? MediaType.LINK : mediaType);
        this.urls = urls;
        this.startSec = startSec;
        this.endSec = endSec;
    }

    public static LinksEntity from(Links links) {
        return new LinksEntity(
                links.getId(),
                links.getUrls(),
                links.getStartSec(),
                links.getEndSec(),
                links.getMediaType()
        );
    }

    @Override
    public Links toModel() {
        return Links.builder()
                .id(super.getId())
                .urls(this.urls)
                .startSec(this.startSec)
                .endSec(this.endSec)
                .mediaType(super.mediaType)
                .build();
    }

    public void update(Links links) {
        this.urls = links.getUrls();
        this.startSec = links.getStartSec();
        this.endSec = links.getEndSec();
    }
}

