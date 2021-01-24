package ru.pushkin.mmb.mapper;

import org.springframework.stereotype.Component;
import ru.pushkin.mmb.api.output.dto.TrackDto;
import ru.pushkin.mmb.data.model.library.PlaylistTrack;
import ru.pushkin.mmb.data.model.library.TrackData;
import ru.pushkin.mmb.utils.DateTimeUtils;

import java.util.Optional;


@Component
public class TrackDataMapper {

    public TrackData map(ru.pushkin.mmb.lastfm.model.Track source) {
        TrackData result = new TrackData();
        result.setTrackName(source.getName());
        result.setArtist(source.getArtist().getName());
        result.setTitle(result.getArtist(), result.getTrackName());
        Optional.ofNullable(source.getAlbum())
                .ifPresent(album -> result.setAlbum(album.getName()));
        Optional.ofNullable(source.getMbid())
                .ifPresent(result::setMbid);
        Optional.ofNullable(source.getDate())
                .ifPresent(date -> result.setDateTime(DateTimeUtils.toLocalDateTime(source.getDate().getUts())));
        Optional.ofNullable(source.getUrl())
                .ifPresent(result::setLastFmUrl);
        return result;
    }

    public TrackData map(ru.pushkin.mmb.lastfm.model.TrackInfo source) {
        TrackData result = new TrackData();
        result.setTrackName(source.getName());
        result.setArtist(source.getArtist().getName());
        result.setTitle(result.getArtist(), result.getTrackName());
        Optional.ofNullable(source.getAlbum())
                .ifPresent(album -> result.setAlbum(album.getTitle()));
        Optional.ofNullable(source.getMbid())
                .ifPresent(result::setMbid);
        Optional.ofNullable(source.getDate())
                .ifPresent(date -> result.setDateTime(DateTimeUtils.toLocalDateTime(date.getUts())));
        Optional.ofNullable(source.getUrl())
                .ifPresent(result::setLastFmUrl);
        return result;
    }

    public TrackData map(ru.pushkin.mmb.deezer.model.Track source) {
        TrackData result = new TrackData();
        result.setTrackName(source.getTitle());
        Optional.ofNullable(source.getArtist())
                .ifPresent(artist -> result.setArtist(artist.getName()));
        result.setTitle(result.getArtist(), result.getTrackName());
        Optional.ofNullable(source.getAlbum())
                .ifPresent(album -> result.setAlbum(album.getTitle()));
        Optional.ofNullable(source.getLink())
                .ifPresent(result::setDeezerUrl);
        Optional.ofNullable(source.getTime_add())
                .ifPresent(date -> result.setDateTime(DateTimeUtils.toLocalDateTime(date.getTime())));
        return result;
    }

    public TrackDto map(TrackData source) {
        return TrackDto.builder()
                .id(source.getId())
                .mbid(source.getMbid())
                .artist(source.getArtist())
                .trackName(source.getTrackName())
                .album(source.getAlbum())
                .length(source.getLength())
                .date(source.getDateTime())
                .sourceLink(source.getLastFmUrl())
                .build();
    }


    public TrackDto map(PlaylistTrack source) {
        TrackData trackData = source.getTrackData();
        return TrackDto.builder()
                .id(trackData.getId())
                .mbid(trackData.getMbid())
                .artist(trackData.getArtist())
                .trackName(trackData.getTrackName())
                .album(trackData.getAlbum())
                .length(trackData.getLength())
                .date(source.getAdded())
                .sourceLink(trackData.getLastFmUrl())
                .position(source.getPosition())
                .build();
    }
}
