package com.games.balancegameback.infra.entity;

import com.games.balancegameback.core.enums.MediaType;
import com.games.balancegameback.core.enums.UsingType;
import jakarta.persistence.*;
import lombok.Getter;
import org.springframework.data.annotation.CreatedDate;

import java.time.LocalDateTime;

@Getter
@Entity
public class Media {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private MediaType mediaType;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private UsingType usingType;

    @Column
    @CreatedDate
    private LocalDateTime createdDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "users_id")
    private Users user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "games_id")
    private Games game;
}

