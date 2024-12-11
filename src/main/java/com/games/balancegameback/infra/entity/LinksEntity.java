package com.games.balancegameback.infra.entity;

import com.games.balancegameback.domain.media.Links;
import com.games.balancegameback.domain.media.Media;
import jakarta.persistence.*;
import lombok.Getter;
import org.springframework.data.annotation.CreatedDate;

import java.time.LocalDateTime;

@Getter
@Entity
@Table(name = "links")
public class LinksEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String urls;

    @Column
    private int startSec;

    @Column
    private int endSec;

    @Column(updatable = false)
    @CreatedDate
    private LocalDateTime createdDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "media_id", nullable = false)
    private MediaEntity media;

    public static LinksEntity from(Links links) {
        LinksEntity linksEntity = new LinksEntity();
        linksEntity.urls = links.urls();
        linksEntity.startSec = links.startSec();
        linksEntity.endSec = links.endSec();
        linksEntity.media = MediaEntity.from(links.media());

        return linksEntity;
    }

    public Links toModel() {
        return Links.builder()
                .id(id)
                .urls(urls)
                .startSec(startSec)
                .endSec(endSec)
                .media(media.toModel())
                .build();
    }
}
