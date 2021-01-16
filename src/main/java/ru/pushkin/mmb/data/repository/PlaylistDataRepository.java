package ru.pushkin.mmb.data.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.pushkin.mmb.data.model.library.PlaylistData;

public interface PlaylistDataRepository extends JpaRepository<PlaylistData, Integer> {
}
