package com.games.balancegameback.infra.repository.user;

import com.games.balancegameback.infra.entity.UsersEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserJpaRepository extends JpaRepository<UsersEntity, Long> {

}
