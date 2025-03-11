package com.games.balancegameback.infra.repository.media;

import com.games.balancegameback.infra.entity.MediaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MediaJpaRepository extends JpaRepository<MediaEntity, Long> {
    
}
