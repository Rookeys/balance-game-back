package com.games.balancegameback.infra.entity;

import com.games.balancegameback.domain.media.Links;
import com.games.balancegameback.domain.media.enums.MediaType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "links")
@NoArgsConstructor
public class LinksEntity extends MediaEntity {

    @Column(nullable = false, length = 500)
    private String urls;

    @Column
    private int startSec;

    @Column
    private int endSec;

    public LinksEntity(String id, String urls, int startSec, int endSec, MediaType mediaType) {
        super(id, mediaType == null ? MediaType.LINK : mediaType);
        this.urls = urls;
        this.startSec = startSec;
        this.endSec = endSec;
    }

    @Override
    protected String getEntityPrefix() {
        return "LNK";
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
                .id(this.id)
                .urls(this.urls)
                .startSec(this.startSec)
                .endSec(this.endSec)
                .mediaType(this.mediaType)
                .build();
    }

    public void update(Links links) {
        this.urls = links.getUrls();
        this.startSec = links.getStartSec();
        this.endSec = links.getEndSec();
    }
}

