package com.games.balancegameback.service.user.impl;

import com.games.balancegameback.core.exception.ErrorCode;
import com.games.balancegameback.core.exception.impl.NotFoundException;
import com.games.balancegameback.core.exception.impl.UnAuthorizedException;
import com.games.balancegameback.domain.user.Users;
import com.games.balancegameback.dto.user.SignUpRequest;
import com.games.balancegameback.service.user.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserManagementService {

    private final UserRepository userRepository;
    private final UserUtils userUtils;
    private final AuthService authService;

    public boolean existsByNickname(String nickname) {
        return userRepository.existsByNickname(nickname);
    }

    @Transactional
    public void signUp(SignUpRequest signUpRequest, HttpServletResponse response) {
        userUtils.validateToken(signUpRequest.getCode(), signUpRequest.getLoginType());

        if (this.existsByNickname(signUpRequest.getNickname())) {
            throw new UnAuthorizedException("중복된 닉네임입니다.", ErrorCode.DUPLICATED_EXCEPTION);
        }

        Users users = signUpRequest.toDomain();
        userRepository.save(users);

        userUtils.createToken(users, response);
    }

    @Transactional
    public void resign(HttpServletRequest request) {
        Users user = userUtils.findUserByToken(request);
        user.setDeleted(true);

        userRepository.update(user);
        authService.logout(request);
    }

    @Transactional
    public void cancelResign(String email) {
        if (userRepository.existsByEmailAndDeleted(email, true)) {
            Users user = userRepository.findByEmail(email).orElseThrow(()
                    -> new NotFoundException("404", ErrorCode.NOT_FOUND_EXCEPTION));

            user.setDeleted(false);
            userRepository.update(user);
        } else {
            throw new UnAuthorizedException("회원 탈퇴한 유저입니다.", ErrorCode.NOT_ALLOW_RESIGN_EXCEPTION);
        }
    }
}

