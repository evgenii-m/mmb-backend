package ru.pushkin.mma.mapper;

import org.springframework.stereotype.Component;
import ru.pushkin.mma.api.output.dto.TrackDto;
import ru.pushkin.mma.deezer.model.Track;

import java.time.LocalDateTime;
import java.time.ZoneId;


@Component
public class TrackMapper {

    public TrackDto map(Track source) {
        return TrackDto.builder()
                .uuid(source.getId().toString())
                .number(source.getTrack_position())
                .artist(source.getArtist().getName())
                .title(source.getTitle())
                .album(source.getAlbum().getTitle())
                .length(source.getDuration())
                .added(LocalDateTime.ofInstant(source.getTime_add().toInstant(), ZoneId.systemDefault()))
                .sourceLink(source.getLink())
                .build();
    }
}
