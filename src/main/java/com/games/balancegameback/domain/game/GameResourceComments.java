package com.games.balancegameback.domain.game;

import com.games.balancegameback.domain.user.Users;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class GameResourceComments {

    private Long id;
    private String comment;
    private boolean like;
    private Users users;
    private GameResources gameResources;
    private Long parentId;
    private LocalDateTime createdDate;
    private LocalDateTime updatedDate;
    private List<GameResourceComments> children;

    @Builder
    public GameResourceComments(Long id, String comment, boolean like, Users users,
                                GameResources gameResources, Long parentId,
                                LocalDateTime createdDate, LocalDateTime updatedDate,
                                List<GameResourceComments> children) {
        this.id = id;
        this.comment = comment;
        this.like = like;
        this.users = users;
        this.gameResources = gameResources;
        this.parentId = parentId;
        this.createdDate = createdDate;
        this.updatedDate = updatedDate;
        this.children = children;
    }
}
