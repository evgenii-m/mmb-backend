package ru.pushkin.mmb.data.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.pushkin.mmb.data.model.library.UserTrackInfo;

import java.util.Optional;


@Repository
public interface UserTrackInfoRepository extends JpaRepository<UserTrackInfo, Integer> {

    Optional<UserTrackInfo> findByTrackIdAndUserId(Integer trackId, String userId);
}
