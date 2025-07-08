package com.games.balancegameback.infra.repository.media;

import com.games.balancegameback.infra.entity.ImagesEntity;
import com.games.balancegameback.infra.entity.UsersEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ImageJpaRepository extends JpaRepository<ImagesEntity, Long> {

    ImagesEntity findByUsers(UsersEntity users);

    List<ImagesEntity> findByGamesId(Long id);

    void deleteByUsersUid(String uid);

    @Query("SELECT i FROM ImagesEntity i WHERE i.users.uid = :uid")
    List<ImagesEntity> findByUsersUid(@Param("uid") String uid);

    @Query("SELECT i.fileUrl FROM ImagesEntity i WHERE i.users.uid = :uid AND i.fileUrl IS NOT NULL")
    List<String> findFileUrlsByUsersUid(@Param("uid") String uid);

    @Query("SELECT i.fileUrl FROM ImagesEntity i WHERE i.games.id = :gameId AND i.fileUrl IS NOT NULL")
    List<String> findFileUrlsByGamesId(@Param("gameId") Long gameId);
}
