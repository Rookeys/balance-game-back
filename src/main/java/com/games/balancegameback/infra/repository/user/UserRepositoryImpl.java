package com.games.balancegameback.infra.repository.user;

import com.games.balancegameback.core.exception.ErrorCode;
import com.games.balancegameback.core.exception.impl.NotFoundException;
import com.games.balancegameback.domain.user.Users;
import com.games.balancegameback.infra.entity.QUsersEntity;
import com.games.balancegameback.infra.entity.UsersEntity;
import com.games.balancegameback.service.user.UserRepository;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class UserRepositoryImpl implements UserRepository {

    private final UserJpaRepository userRepository;
    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public Users findByEmail(String email) {
        UsersEntity users = userRepository.findByEmail(email).orElseThrow(()
                -> new NotFoundException("해당 이메일을 가진 유저를 찾을 수 없습니다.", ErrorCode.NOT_FOUND_EXCEPTION));
        return users.toModel();
    }

    @Override
    public Users findByNickname(String nickname) {
        UsersEntity users = userRepository.findByNickname(nickname).orElseThrow(()
                -> new NotFoundException("해당 닉네임을 가진 유저를 찾을 수 없습니다.", ErrorCode.NOT_FOUND_EXCEPTION));
        return users.toModel();
    }

    @Override
    public Optional<Users> findByUserEmail(String email) {
        Optional<UsersEntity> users = userRepository.findByEmail(email);
        return users.map(UsersEntity::toModel);
    }

    @Override
    public Users save(Users users) {
        UsersEntity usersEntity = userRepository.save(UsersEntity.from(users));
        return usersEntity.toModel();
    }

    @Override
    public void update(Users users) {
        UsersEntity usersEntity = userRepository.findByEmail(users.getEmail()).orElseThrow();
        usersEntity.update(users);
    }

    @Override
    public boolean existsByNickname(String nickname) {
        return userRepository.existsByNickname(nickname);
    }
}
