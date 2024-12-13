package com.games.balancegameback.infra.repository.user;

import com.games.balancegameback.domain.user.Users;
import com.games.balancegameback.infra.entity.UsersEntity;
import com.games.balancegameback.service.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class UserRepositoryImpl implements UserRepository {

    private final UserJpaRepository userRepository;

    @Override
    public Optional<Users> findByEmail(String email) {
        Optional<UsersEntity> users = userRepository.findByEmail(email);
        return users.map(UsersEntity::toModel);
    }

    @Override
    public void save(Users users) {
        userRepository.save(UsersEntity.from(users));
    }
}
