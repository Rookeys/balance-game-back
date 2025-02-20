package com.games.balancegameback.service.game.impl.comment;

import com.games.balancegameback.domain.game.enums.CommentType;
import com.games.balancegameback.service.user.impl.UserUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Duration;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.ScanOptions;

import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class GameCommentLikesService {

    private static final String LIKE_PREFIX = "game:like:";
    private static final Duration EXPIRATION = Duration.ofMinutes(1);

    private final UserUtils userUtils;
    private final StringRedisTemplate redisTemplate;

    /**
     * 좋아요 토글 요청 시 Redis에 저장
     * isLiked -> true (좋아요) / false (취소)
     */
    @Transactional
    public void toggleLike(Long commentId, boolean isLiked, CommentType commentType, HttpServletRequest request) {
        String email = userUtils.getEmail(request);
        String key = getRedisKey(email, commentId, commentType.equals(CommentType.RESOURCE));

        redisTemplate.opsForValue().set(key, String.valueOf(isLiked), EXPIRATION);
    }

    public boolean getFinalLikeState(String email, Long commentId, boolean isResourceComment) {
        String finalKey = getRedisKey(email, commentId, isResourceComment);
        return Objects.equals(redisTemplate.opsForValue().get(finalKey), "true");
    }

    public Set<String> getAllLikeKeys() {
        Set<String> keys = new HashSet<>();
        ScanOptions options = ScanOptions.scanOptions().match(LIKE_PREFIX + "*").count(50).build();

        redisTemplate.executeWithStickyConnection(connection -> {
            Cursor<byte[]> cursor = connection.keyCommands().scan(options);
            while (cursor.hasNext()) {
                keys.add(new String(cursor.next(), StandardCharsets.UTF_8));
            }

            return null;
        });

        return keys;
    }

    private String getRedisKey(String email, Long commentId, boolean isResourceComment) {
        return LIKE_PREFIX + (isResourceComment ? "resource:" : "result:") + email + ":" + commentId;
    }
}

