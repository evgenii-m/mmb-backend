package ru.pushkin.mmb.data.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.pushkin.mmb.data.model.library.TrackData;

import java.util.Collection;
import java.util.List;

@Repository
public interface TrackDataRepository extends JpaRepository<TrackData, Integer> {

    List<TrackData> findAllByMbidInOrTitleIn(Collection<String> mbids, Collection<String> titles);
}
