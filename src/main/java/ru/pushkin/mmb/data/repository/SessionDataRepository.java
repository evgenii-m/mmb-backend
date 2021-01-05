package ru.pushkin.mmb.data.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.pushkin.mmb.data.model.SessionData;

import java.util.List;

@Repository
public interface SessionDataRepository extends JpaRepository<SessionData, Integer> {

    List<SessionData> findByCodeAndUserId(String code, String userId);
}
