package com.games.balancegameback.core.config;

import com.games.balancegameback.service.game.impl.comment.GameCommentUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

@Configuration
@EnableScheduling
@RequiredArgsConstructor
public class SchedulerConfig {

    private final GameCommentUtils gameCommentUtils;

    @Scheduled(fixedDelay = 60000) // 1분마다 실행
    public void syncLikesToDB() {
        gameCommentUtils.processLikes();
    }
}

