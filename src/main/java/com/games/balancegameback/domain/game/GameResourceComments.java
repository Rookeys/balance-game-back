package com.games.balancegameback.domain.game;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class GameResourceComments {

    private Long id;
    private String comment;
    private GameResources gameResources;
    private Long parentId;
    private LocalDateTime createdDate;
    private LocalDateTime updatedDate;
    private List<GameResourceComments> children;

    @Builder
    public GameResourceComments(Long id, String comment, GameResources gameResources, Long parentId,
                                LocalDateTime createdDate, LocalDateTime updatedDate,
                                List<GameResourceComments> children) {
        this.id = id;
        this.comment = comment;
        this.gameResources = gameResources;
        this.parentId = parentId;
        this.createdDate = createdDate;
        this.updatedDate = updatedDate;
        this.children = children;
    }
}
