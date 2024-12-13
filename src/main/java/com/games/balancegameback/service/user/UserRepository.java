package com.games.balancegameback.service.user;

import com.games.balancegameback.domain.user.Users;

import java.util.Optional;

public interface UserRepository {

    Optional<Users> findByEmail(String email);

    void save(Users users);
}
