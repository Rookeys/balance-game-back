package com.games.balancegameback.service.user.repository;

import com.games.balancegameback.domain.user.Users;

import java.util.List;
import java.util.Optional;

public interface UserRepository {

    Users findByEmail(String email);

    Users findByNickname(String nickname);
    
    List<Users> findByUids(List<String> uids);

    Optional<Users> findByUserEmail(String email);

    Users save(Users users);

    void update(Users users);

    void delete(Users users);

    boolean existsByNickname(String nickname);

    List<Users> findRandomUsersExcludingUids(String userUid, List<String> excludeUids, int limit);
}
