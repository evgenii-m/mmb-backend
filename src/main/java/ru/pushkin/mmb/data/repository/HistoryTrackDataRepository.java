package ru.pushkin.mmb.data.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import ru.pushkin.mmb.data.model.library.HistoryTrackData;

public interface HistoryTrackDataRepository extends MongoRepository<HistoryTrackData, String> {

}
