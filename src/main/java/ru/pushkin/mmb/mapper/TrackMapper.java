package ru.pushkin.mmb.mapper;

import org.springframework.stereotype.Component;
import ru.pushkin.mmb.api.output.dto.TrackDto;
import ru.pushkin.mmb.deezer.model.Track;
import ru.pushkin.mmb.lastfm.model.Album;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;
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

    public TrackDto map(ru.pushkin.mmb.lastfm.model.TrackInfo source) {
        return TrackDto.builder()
                .uuid(source.getMbid())
                .artist(source.getArtist().getName())
                .title(source.getName())
                .album(Optional.ofNullable(source.getAlbum()).map(Album::getTitle).orElse(null))
                .added(
                        LocalDateTime.ofInstant(Instant.ofEpochSecond(source.getDate().getUts()), TimeZone.getDefault().toZoneId())
                )
                .build();
    }

}
