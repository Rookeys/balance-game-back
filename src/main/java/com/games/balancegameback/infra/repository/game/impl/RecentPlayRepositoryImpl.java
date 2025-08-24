package com.games.balancegameback.infra.repository.game.impl;

import com.games.balancegameback.core.utils.CustomPageImpl;
import com.games.balancegameback.domain.game.RecentPlay;
import com.games.balancegameback.domain.game.enums.AccessType;
import com.games.balancegameback.domain.game.enums.Category;
import com.games.balancegameback.domain.user.Users;
import com.games.balancegameback.dto.game.RecentPlayListResponse;
import com.games.balancegameback.infra.entity.RecentPlayEntity;
import com.games.balancegameback.infra.repository.game.RecentPlayJpaRepository;
import com.games.balancegameback.infra.repository.game.common.CommonGameRepository;
import com.games.balancegameback.infra.repository.game.common.GameBatchData;
import com.games.balancegameback.infra.repository.game.common.GameQClasses;
import com.games.balancegameback.service.game.repository.RecentPlayRepository;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.Tuple;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Log4j2
@Repository
@RequiredArgsConstructor
public class RecentPlayRepositoryImpl implements RecentPlayRepository {

    private final RecentPlayJpaRepository recentPlayJpaRepository;
    private final CommonGameRepository commonGameRepository;
    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public Long save(RecentPlay recentPlay) {
        return recentPlayJpaRepository.save(RecentPlayEntity.from(recentPlay)).getId();
    }

    @Override
    public Long updateRecentPlay(RecentPlay recentPlay) {
        RecentPlayEntity entity = RecentPlayEntity.from(recentPlay);
        return recentPlayJpaRepository.save(entity).getId();
    }

    @Override
    public void delete(RecentPlay recentPlay) {
        recentPlayJpaRepository.delete(RecentPlayEntity.from(recentPlay));
    }

    @Override
    public Optional<RecentPlay> findByUserUidAndGameId(String userUid, Long gameId) {
        return recentPlayJpaRepository.findByUserUidAndGameId(userUid, gameId).map(RecentPlayEntity::toModel);
    }

    @Override
    public long countByUserUid(String userUid) {
        return recentPlayJpaRepository.countByUserUid(userUid);
    }

    @Override
    public Optional<RecentPlay> findOldestByUserUid(String userUid) {
        return recentPlayJpaRepository.findOldestByUserUid(userUid).map(RecentPlayEntity::toModel);
    }

    @Override
    public CustomPageImpl<RecentPlayListResponse> getRecentPlayList(Long cursorId, Pageable pageable, Users user) {
        try {
            if (user == null) {
                return new CustomPageImpl<>(Collections.emptyList(), pageable, 0L, cursorId, false);
            }

            // 총 개수 조회
            Long totalElements = calculateRecentPlayTotalElements(user);

            List<Tuple> recentPlayTuples = fetchRecentPlayTuples(user, pageable, cursorId);

            if (recentPlayTuples.isEmpty()) {
                return new CustomPageImpl<>(Collections.emptyList(), pageable, totalElements, cursorId, false);
            }

            List<RecentPlayListResponse> responses = buildRecentPlayResponses(recentPlayTuples);

            boolean hasNext = responses.size() > pageable.getPageSize();
            if (hasNext) {
                responses.removeLast();
            }

            return new CustomPageImpl<>(responses, pageable, totalElements, cursorId, hasNext);
        } catch (Exception e) {
            log.error("Error in getRecentPlayList", e);
            return new CustomPageImpl<>(Collections.emptyList(), pageable, 0L, cursorId, false);
        }
    }

    // =========================== 전용 조회 메서드들 ===========================

    /**
     * 최근 플레이 게임의 총 개수 계산
     */
    private Long calculateRecentPlayTotalElements(Users user) {
        try {
            BooleanBuilder conditions = createBaseConditions(user);

            return (long) jpaQueryFactory
                .selectFrom(GameQClasses.recentPlay)
                .join(GameQClasses.games).on(GameQClasses.recentPlay.gameId.eq(GameQClasses.games.id))
                .leftJoin(GameQClasses.games.gameResources, GameQClasses.resources)
                .where(conditions)
                .fetch()
                .size();

        } catch (Exception e) {
            log.error("Error calculating recent play total elements for user: {}", user.getUid(), e);
            return 0L;
        }
    }

    /**
     * recent_plays 테이블을 기반으로 최근 플레이 게임 데이터 조회
     */
    private List<Tuple> fetchRecentPlayTuples(Users user, Pageable pageable, Long cursorId) {
        BooleanBuilder conditions = createBaseConditions(user);

        if (cursorId != null) {
            conditions.and(GameQClasses.recentPlay.id.lt(cursorId));
        }

        return jpaQueryFactory
                .select(GameQClasses.games.id,                        // 0
                        GameQClasses.games.title,                            // 1
                        GameQClasses.games.description,                      // 2
                        GameQClasses.resources.title,                        // 3
                        GameQClasses.games.isBlind,                          // 4
                        GameQClasses.recentPlay.id,                          // 5 (커서용 ID)
                        GameQClasses.recentPlay.resourceId,                  // 6 (resourceId)
                        GameQClasses.images.fileUrl,                         // 7 (리소스 이미지 URL)
                        GameQClasses.links.urls,                             // 8 (리소스 유튜브 URL)
                        GameQClasses.images.mediaType,                       // 9 (이미지 미디어 타입)
                        GameQClasses.links.mediaType                         // 10 (링크 미디어 타입)
                )
                .from(GameQClasses.recentPlay)
                .join(GameQClasses.games).on(GameQClasses.recentPlay.gameId.eq(GameQClasses.games.id))
                .leftJoin(GameQClasses.resources).on(GameQClasses.resources.id.eq(GameQClasses.recentPlay.resourceId))
                .leftJoin(GameQClasses.resources.images, GameQClasses.images)
                .leftJoin(GameQClasses.resources.links, GameQClasses.links)
                .where(conditions)
                .orderBy(GameQClasses.recentPlay.updatedDate.desc(), GameQClasses.recentPlay.id.desc())
                .limit(pageable.getPageSize() + 1)
                .fetch();
    }

    /**
     * 최근 플레이 응답 리스트 생성
     */
    private List<RecentPlayListResponse> buildRecentPlayResponses(List<Tuple> tuples) {
        List<Long> gameIds = tuples.stream()
                .map(tuple -> tuple.get(GameQClasses.games.id))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        // Common 레포지토리의 배치 조회 활용
        GameBatchData batchData = commonGameRepository.getTotalPlayBatchData(gameIds);

        return tuples.stream()
                .map(tuple -> buildRecentPlayResponse(tuple, batchData))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    /**
     * 개별 최근 플레이 응답 생성
     */
    private RecentPlayListResponse buildRecentPlayResponse(Tuple tuple, GameBatchData batchData) {
        Long roomId = tuple.get(GameQClasses.games.id);
        if (roomId == null) {
            return null;
        }

        // 배치 데이터에서 가져오기
        List<Category> categories = batchData.getCategoriesMap().getOrDefault(roomId, Collections.emptyList());

        // 썸네일 정보 처리
        String thumbnailImageUrl = tuple.get(7, String.class);  // 리소스 이미지 URL
        String thumbnailLinkUrl = tuple.get(8, String.class);  // 리소스 유튜브 URL

        if (!StringUtils.hasText(thumbnailImageUrl)) {
            thumbnailImageUrl = null;
        }
        if (!StringUtils.hasText(thumbnailLinkUrl)) {
            thumbnailLinkUrl = null;
        }

        // 썸네일 타입 결정
        Object imageMediaType = tuple.get(9, Objects.class);    // 이미지 미디어 타입
        Object linkMediaType = tuple.get(10, Objects.class);    // 링크 미디어 타입

        String thumbnailType = null;
        if (StringUtils.hasText(thumbnailImageUrl) && imageMediaType != null) {
            thumbnailType = imageMediaType.toString();
        } else if (StringUtils.hasText(thumbnailLinkUrl) && linkMediaType != null) {
            thumbnailType = linkMediaType.toString();
        }

        return RecentPlayListResponse.builder()
                .roomId(roomId)
                .title(tuple.get(GameQClasses.games.title))
                .resourceTitle(tuple.get(GameQClasses.resources.title))
                .description(tuple.get(GameQClasses.games.description))
                .categories(categories)
                .existsBlind(tuple.get(GameQClasses.games.isBlind))
                .thumbnailImageUrl(thumbnailImageUrl)
                .thumbnailLinkUrl(thumbnailLinkUrl)
                .thumbnailType(thumbnailType)
                .build();
    }

    // =========================== 헬퍼 메서드들 ===========================

    /**
     * 기본 조건 생성
     */
    private BooleanBuilder createBaseConditions(Users user) {
        BooleanBuilder conditions = new BooleanBuilder();

        // 해당 사용자의 최근 플레이 기록
        conditions.and(GameQClasses.recentPlay.userUid.eq(user.getUid()));

        // 접근 가능한 게임만 (공개 게임 + 내가 만든 게임)
        conditions.and(GameQClasses.games.accessType.ne(AccessType.PRIVATE)
                .or(GameQClasses.games.users.uid.eq(user.getUid())));

        return conditions;
    }
}
