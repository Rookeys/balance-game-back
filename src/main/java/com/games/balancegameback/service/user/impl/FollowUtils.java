package com.games.balancegameback.service.user.impl;

import com.games.balancegameback.core.exception.ErrorCode;
import com.games.balancegameback.core.exception.impl.NotFoundException;
import com.games.balancegameback.domain.user.Users;
import com.games.balancegameback.service.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class FollowUtils {

    private final UserRepository userRepository;

    /**
     * 이메일로 UID 조회
     */
    public String getUserUidByEmail(String email) {
        try {
            Users users = userRepository.findByEmail(email);
            return users.getUid();
        } catch (NotFoundException e) {
            throw new NotFoundException("존재하지 않는 사용자입니다.", ErrorCode.NOT_FOUND_EXCEPTION);
        }
    }

    /**
     * 닉네임으로 UID 조회
     */
    public String getUserUidByNickname(String nickname) {
        try {
            Users users = userRepository.findByNickname(nickname);
            return users.getUid();
        } catch (NotFoundException e) {
            throw new NotFoundException("존재하지 않는 사용자입니다.", ErrorCode.NOT_FOUND_EXCEPTION);
        }
    }
}
