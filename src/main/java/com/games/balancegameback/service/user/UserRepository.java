package com.games.balancegameback.service.user;

import com.games.balancegameback.domain.user.Users;

import java.util.Optional;

public interface UserRepository {

    Users findByEmail(String email);

    Users findByNickname(String nickname);

    Optional<Users> findByUserEmail(String email);

    Users save(Users users);

    void update(Users users);

    boolean existsByNickname(String nickname);
}
