package ru.pushkin.mmb.data.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.pushkin.mmb.data.model.library.TagData;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface TagDataRepository extends JpaRepository<TagData, Integer> {

    Optional<TagData> findByName(String name);

    List<TagData> findByNameIn(Collection<String> names);
}
