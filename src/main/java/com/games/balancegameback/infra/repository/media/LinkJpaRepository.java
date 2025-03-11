package com.games.balancegameback.infra.repository.media;

import com.games.balancegameback.infra.entity.LinksEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LinkJpaRepository extends JpaRepository<LinksEntity, Long> {

}
