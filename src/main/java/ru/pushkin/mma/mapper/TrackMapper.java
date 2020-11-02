package ru.pushkin.mma.mapper;

import org.springframework.stereotype.Component;
import ru.pushkin.mma.api.output.dto.TrackDto;
import ru.pushkin.mma.deezer.model.Track;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.TimeZone;


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
                .added(
                        LocalDateTime.ofInstant(Instant.ofEpochSecond(source.getTime_add().getTime()), TimeZone.getDefault().toZoneId())
                )
                .sourceLink(source.getLink())
                .build();
    }
}
