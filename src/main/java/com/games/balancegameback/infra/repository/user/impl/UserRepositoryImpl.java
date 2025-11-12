package com.games.balancegameback.infra.repository.user.impl;

import com.games.balancegameback.core.exception.ErrorCode;
import com.games.balancegameback.core.exception.impl.NotFoundException;
import com.games.balancegameback.domain.user.Users;
import com.games.balancegameback.infra.entity.UsersEntity;
import com.games.balancegameback.infra.repository.user.UserJpaRepository;
import com.games.balancegameback.service.user.repository.UserRepository;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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
    public List<Users> findByUids(List<String> uids) {
        List<UsersEntity> usersEntities = userRepository.findByUidIn(uids);
        return usersEntities.stream()
                .map(UsersEntity::toModel)
                .collect(Collectors.toList());
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
    public void delete(Users users) {
        userRepository.delete(UsersEntity.from(users));
    }

    @Override
    public boolean existsByNickname(String nickname) {
        return userRepository.existsByNickname(nickname);
    }
    
    @Override
    public List<Users> findRandomUsersExcludingUids(String userUid, List<String> excludeUids, int limit) {
        List<String> allExcludeUids = new ArrayList<>();
        
        // 자신의 UID 제외
        if (userUid != null) {
            allExcludeUids.add(userUid);
        }
        
        // 팔로우 중인 사용자들 제외
        if (excludeUids != null && !excludeUids.isEmpty()) {
            allExcludeUids.addAll(excludeUids);
        }
        
        // 모든 사용자 조회
        List<UsersEntity> allUsers = userRepository.findAll().stream()
                .filter(user -> !user.getIsDeleted())
                .filter(user -> !allExcludeUids.contains(user.getUid()))
                .collect(Collectors.toList());
        
        // 랜덤으로 섞기
        Collections.shuffle(allUsers);

        return allUsers.stream()
                .limit(limit)
                .map(UsersEntity::toModel)
                .collect(Collectors.toList());
    }
}
