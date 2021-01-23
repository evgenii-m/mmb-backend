package ru.pushkin.mmb.data.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.pushkin.mmb.data.enumeration.PlaylistType;
import ru.pushkin.mmb.data.model.library.PlaylistData;

import java.util.Collection;
import java.util.List;

@Repository
public interface PlaylistDataRepository extends JpaRepository<PlaylistData, Integer> {

    List<PlaylistData> findAllBySourceUrlInAndTypeAndUserId(Collection<String> sourceUrl, PlaylistType type, String userId);
}
