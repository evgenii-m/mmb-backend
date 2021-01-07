package ru.pushkin.mmb.data.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.pushkin.mmb.data.model.library.TrackData;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface TrackDataRepository extends JpaRepository<TrackData, Integer> {

    Optional<TrackData> findByMbidOrTitle(String mbid, String title);

    Optional<TrackData> findByMbid(String mbid);

    Optional<TrackData> findByTitle(String title);

    List<TrackData> findAllByMbidInOrTitleIn(Collection<String> mbids, Collection<String> titles);
}
