package ru.pushkin.mmb.data.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import ru.pushkin.mmb.data.model.library.TrackData;

import java.util.Collection;
import java.util.List;

public interface TrackDataRepository extends MongoRepository<TrackData, String> {

    List<TrackData> findAllByMbidInOrTitleIn(Collection<String> mbids, Collection<String> titles);
}
