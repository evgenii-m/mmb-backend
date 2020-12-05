package ru.pushkin.mmb.data.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import ru.pushkin.mmb.data.model.SessionData;

import java.util.List;

@Repository
public interface SessionDataRepository extends MongoRepository<SessionData, String> {

    List<SessionData> findByCodeAndUserId(String code, String userId);
}
