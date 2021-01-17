package ru.pushkin.mmb.data.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.pushkin.mmb.data.model.library.PlaylistData;

@Repository
public interface PlaylistDataRepository extends JpaRepository<PlaylistData, Integer> {
}
