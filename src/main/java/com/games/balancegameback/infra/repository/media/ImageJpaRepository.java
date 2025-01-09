package com.games.balancegameback.infra.repository.media;

import com.games.balancegameback.infra.entity.ImagesEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ImageJpaRepository extends JpaRepository<ImagesEntity, Long> {


}
