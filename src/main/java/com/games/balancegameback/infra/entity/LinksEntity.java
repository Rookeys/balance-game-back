package com.games.balancegameback.infra.entity;

import com.games.balancegameback.domain.media.Links;
import jakarta.persistence.*;
import lombok.Getter;

@Getter
@Entity
@DiscriminatorValue("LINK")
@Table(name = "links")
public class LinksEntity extends MediaEntity {

    @Column(nullable = false)
    private String urls;

    @Column
    private int startSec;

    @Column
    private int endSec;

    public static LinksEntity from(Links links) {
        LinksEntity linksEntity = new LinksEntity();
        linksEntity.urls = links.getUrls();
        linksEntity.startSec = links.getStartSec();
        linksEntity.endSec = links.getEndSec();

        if (links.getGames() != null) {
            linksEntity.games = GamesEntity.from(links.getGames());
        }

        return linksEntity;
    }

    @Override
    public Links toModel() {
        return Links.builder()
                .id(this.getId())
                .urls(urls)
                .startSec(startSec)
                .endSec(endSec)
                .build();
    }
}
