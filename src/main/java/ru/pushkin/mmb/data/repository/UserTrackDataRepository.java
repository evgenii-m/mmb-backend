package ru.pushkin.mmb.data.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.pushkin.mmb.data.model.library.UserTrackData;

import java.util.Optional;


@Repository
public interface UserTrackDataRepository extends JpaRepository<UserTrackData, Integer> {

    Optional<UserTrackData> findByTrackIdAndUserId(Integer trackId, String userId);
}
