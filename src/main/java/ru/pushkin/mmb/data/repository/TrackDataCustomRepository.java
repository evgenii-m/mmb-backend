package ru.pushkin.mmb.data.repository;

import ru.pushkin.mmb.data.model.library.TrackData;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface TrackDataCustomRepository {

    List<TrackData> findAllByMbidOrTitle(Collection<String> mbids, Collection<String> titles, String userId);

    Optional<TrackData> findByMbidOrTitle(String mbid, String title, String userId);

    Optional<TrackData> findByMbid(String mbid, String userId);

    Optional<TrackData> findByTitle(String title, String userId);

}
