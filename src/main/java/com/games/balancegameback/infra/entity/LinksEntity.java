package com.games.balancegameback.infra.entity;

import com.games.balancegameback.domain.media.Links;
import com.games.balancegameback.domain.media.Media;
import jakarta.persistence.*;
import lombok.Getter;
import org.springframework.data.annotation.CreatedDate;

import java.time.LocalDateTime;

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
        linksEntity.urls = links.urls();
        linksEntity.startSec = links.startSec();
        linksEntity.endSec = links.endSec();

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
