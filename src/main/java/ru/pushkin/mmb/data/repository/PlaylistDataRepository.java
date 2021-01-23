package ru.pushkin.mmb.data.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.pushkin.mmb.data.enumeration.PlaylistType;
import ru.pushkin.mmb.data.model.library.PlaylistData;

import java.util.Collection;
import java.util.List;

@Repository
public interface PlaylistDataRepository extends JpaRepository<PlaylistData, Integer> {

    List<PlaylistData> findBySourceUrlInAndUserIdAndType(Collection<String> sourceUrl, String userId, PlaylistType type);

    Page<PlaylistData> findByUserIdAndType(String userId, PlaylistType type, Pageable pageable);

    Page<PlaylistData> findByUserIdAndTypeIn(String userId, Collection<PlaylistType> types, Pageable pageable);

    Page<PlaylistData> findByUserId(String userId, Pageable pageable);

    PlaylistData findByIdAndUserId(Integer id, String userId);
}
