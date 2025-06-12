package com.games.balancegameback.infra.entity;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.OffsetDateTime;

@Getter
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class BaseEntity {

    @Id
    @Column(length = 36)
    protected String id;

    @Column(updatable = false)
    @CreatedDate
    private OffsetDateTime createdDate;

    @Column
    @LastModifiedDate
    private OffsetDateTime updatedDate;

    /**
     * 엔티티별 고유 접두사를 반환합니다.
     * 각 엔티티는 이 메서드를 오버라이드하여 고유한 접두사를 제공해야 합니다.
     *
     * @return 엔티티 타입별 고유 접두사
     */
    protected abstract String getEntityPrefix();

    /**
     * UUID 생성 및 접두사 설정
     */
    protected void generateId() {
        if (this.id == null) {
            String uuid = java.util.UUID.randomUUID().toString().replace("-", "");
            this.id = getEntityPrefix() + "_" + uuid.substring(0, 12);
        }
    }
}
