package com.games.balancegameback.infra.repository.game;

import com.games.balancegameback.infra.entity.ReportEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GameReportJpaRepository extends JpaRepository<ReportEntity, String> {

    void deleteByReporterId(String reporterId);
}
