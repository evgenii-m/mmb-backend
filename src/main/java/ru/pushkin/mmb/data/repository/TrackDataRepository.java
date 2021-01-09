package ru.pushkin.mmb.data.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.pushkin.mmb.data.model.library.TrackData;

@Repository
public interface TrackDataRepository extends JpaRepository<TrackData, Integer>, TrackDataCustomRepository {

}
