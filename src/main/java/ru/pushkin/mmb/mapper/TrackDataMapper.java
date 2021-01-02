package ru.pushkin.mmb.mapper;

import org.springframework.stereotype.Component;
import ru.pushkin.mmb.api.output.dto.TrackDto;
import ru.pushkin.mmb.data.model.library.HistoryTrackData;
import ru.pushkin.mmb.lastfm.model.Album;
import ru.pushkin.mmb.lastfm.model.AlbumShort;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.TimeZone;


@Component
public class TrackDataMapper {

    public HistoryTrackData mapHistoryTrackData(ru.pushkin.mmb.lastfm.model.Track source) {
        HistoryTrackData result = new HistoryTrackData();
        result.setTitle(source.getName());
        result.setArtist(source.getArtist().getName());
        Optional.ofNullable(source.getAlbum())
                .ifPresent(album -> result.setAlbum(album.getName()));
        Optional.ofNullable(source.getMbid())
                .ifPresent(mbid -> result.setMbid(mbid));
        Optional.ofNullable(source.getDate())
                .ifPresent(date -> result.setDateTime(toLocalDateTime(source.getDate().getUts())));
        Optional.ofNullable(source.getUrl())
                .ifPresent(lastFmUrl -> result.setLastFmUrl(lastFmUrl));
        return result;
    }

    public TrackDto map(HistoryTrackData source) {
        return TrackDto.builder()
                .uuid(source.getId())
                .artist(source.getArtist())
                .title(source.getTitle())
                .album(source.getAlbum())
                .length(source.getLength())
                .date(source.getDateTime())
                .sourceLink(source.getLastFmUrl())
                .build();
    }

    public TrackDto map(ru.pushkin.mmb.lastfm.model.Track source) {
        return TrackDto.builder()
                .uuid(source.getMbid())
                .artist(source.getArtist().getName())
                .title(source.getName())
                .album(Optional.ofNullable(source.getAlbum()).map(AlbumShort::getName).orElse(null))
                .date(
                        LocalDateTime.ofInstant(Instant.ofEpochSecond(source.getDate().getUts()), TimeZone.getDefault().toZoneId())
                )
                .sourceLink(source.getUrl())
                .build();
    }

    public TrackDto map(ru.pushkin.mmb.lastfm.model.TrackInfo source) {
        return TrackDto.builder()
                .uuid(source.getMbid())
                .artist(source.getArtist().getName())
                .title(source.getName())
                .album(Optional.ofNullable(source.getAlbum()).map(Album::getTitle).orElse(null))
                .date(
                        LocalDateTime.ofInstant(Instant.ofEpochSecond(source.getDate().getUts()), TimeZone.getDefault().toZoneId())
                )
                .sourceLink(source.getUrl())
                .build();
    }

    private static LocalDateTime toLocalDateTime(long utsDate) {
        return LocalDateTime.ofInstant(Instant.ofEpochSecond(utsDate), TimeZone.getDefault().toZoneId());
    }
}
