package com.games.balancegameback.service.user;

import com.games.balancegameback.domain.user.Users;

import java.util.Optional;

public interface UserRepository {

    Optional<Users> findByEmail(String email);

    Users save(Users users);

    void update(Users users);

    boolean existsByEmailAndDeleted(String email, boolean isDeleted);

    boolean existsByNickname(String nickname);
}
