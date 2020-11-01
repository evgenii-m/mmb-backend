package ru.pushkin.mma.data.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import ru.pushkin.mma.data.model.SessionData;

import java.util.List;

@Repository
public interface SessionDataRepository extends MongoRepository<SessionData, Integer> {

    List<SessionData> findByCodeAndUserId(String code, String userId);
}
