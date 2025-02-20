package com.games.balancegameback.domain.game;

import com.games.balancegameback.domain.user.Users;
import com.games.balancegameback.infra.entity.GameCommentLikesEntity;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class GameResourceComments {

    private Long id;
    private String comment;
    private boolean isDeleted;
    private Users users;
    private GameResources gameResources;
    private Long parentId;
    private LocalDateTime createdDate;
    private LocalDateTime updatedDate;
    private List<GameResourceComments> children;
    private List<GameCommentLikesEntity> likes;

    @Builder
    public GameResourceComments(Long id, String comment, Users users, boolean isDeleted, GameResources gameResources,
                                Long parentId, LocalDateTime createdDate, LocalDateTime updatedDate,
                                List<GameResourceComments> children,
                                List<GameCommentLikesEntity> likes) {
        this.id = id;
        this.comment = comment;
        this.isDeleted = isDeleted;
        this.users = users;
        this.gameResources = gameResources;
        this.parentId = parentId;
        this.createdDate = createdDate;
        this.updatedDate = updatedDate;
        this.children = children;
        this.likes = likes;
    }

    public void update(String comment) {
        this.comment = comment;
    }
}
