package ru.pushkin.mmb.data.repository;

import com.querydsl.core.types.ConstructorExpression;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.jpa.repository.support.QuerydslRepositorySupport;
import org.springframework.stereotype.Repository;
import ru.pushkin.mmb.data.model.library.QTrackData;
import ru.pushkin.mmb.data.model.library.QUserTrackInfo;
import ru.pushkin.mmb.data.model.library.TrackData;
import ru.pushkin.mmb.data.model.library.UserTrackInfo;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Repository
public class TrackDataCustomRepositoryImpl extends QuerydslRepositorySupport implements TrackDataCustomRepository {

    public TrackDataCustomRepositoryImpl() {
        super(TrackData.class);
    }

    @Override
    @PersistenceContext(unitName = "mmbPU")
    public void setEntityManager(EntityManager entityManager) {
        super.setEntityManager(entityManager);
    }


    @Override
    public List<TrackData> findAllByMbidOrTitle(Collection<String> mbids, Collection<String> titles, String userId) {
        BooleanExpression whereClause = QTrackData.trackData.mbid.in(mbids)
                .or(QTrackData.trackData.title.in(titles));
        List<TrackDataProjection> result = formQueryForFetchTrackDataWithUserInfo(whereClause, userId).fetch();
        return result.stream()
                .map(entry -> {
                    entry.getTrackData().setUserTrackInfo(entry.getTrackInfo());
                    return entry.getTrackData();
                })
                .collect(Collectors.toList());
    }

    @Override
    public Optional<TrackData> findByMbidOrTitle(String mbid, String title, String userId) {
        BooleanExpression whereClause = QTrackData.trackData.mbid.eq(mbid)
                .or(QTrackData.trackData.title.eq(title));
        TrackDataProjection result = formQueryForFetchTrackDataWithUserInfo(whereClause, userId).fetchOne();
        if (result != null) {
            result.getTrackData().setUserTrackInfo(result.getTrackInfo());
            return Optional.of(result.getTrackData());
        } else {
            return Optional.empty();
        }
    }

    @Override
    public Optional<TrackData> findByMbid(String mbid, String userId) {
        BooleanExpression whereClause = QTrackData.trackData.mbid.eq(mbid);
        TrackDataProjection result = formQueryForFetchTrackDataWithUserInfo(whereClause, userId).fetchOne();
        if (result != null) {
            result.getTrackData().setUserTrackInfo(result.getTrackInfo());
            return Optional.of(result.getTrackData());
        } else {
            return Optional.empty();
        }
    }

    @Override
    public Optional<TrackData> findByTitle(String title, String userId) {
        BooleanExpression whereClause = QTrackData.trackData.title.eq(title);
        TrackDataProjection result = formQueryForFetchTrackDataWithUserInfo(whereClause, userId).fetchOne();
        if (result != null) {
            result.getTrackData().setUserTrackInfo(result.getTrackInfo());
            return Optional.of(result.getTrackData());
        } else {
            return Optional.empty();
        }
    }

    private JPAQuery<TrackDataProjection> formQueryForFetchTrackDataWithUserInfo(BooleanExpression whereClause, String userId) {
        JPAQueryFactory queryFactory = new JPAQueryFactory(getEntityManager());
        ConstructorExpression<TrackDataProjection> projection = Projections.constructor(
                TrackDataProjection.class,
                QTrackData.trackData,
                QUserTrackInfo.userTrackInfo
        );
        return queryFactory.select(projection)
                .from(QTrackData.trackData)
                .join(QUserTrackInfo.userTrackInfo)
                .on(QUserTrackInfo.userTrackInfo.trackId.eq(QTrackData.trackData.id).and(QUserTrackInfo.userTrackInfo.userId.eq(userId)))
                .where(whereClause);
    }

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class TrackDataProjection {
        private TrackData trackData;
        private UserTrackInfo trackInfo;
    }
}
