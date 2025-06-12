package com.games.balancegameback.infra.repository.media;

import com.games.balancegameback.infra.entity.ImagesEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ImageJpaRepository extends JpaRepository<ImagesEntity, String> {

    Optional<ImagesEntity> findByUserId(String userId);

    List<ImagesEntity> findByGameId(String gameId);

    void deleteByUserId(String userId);
}
