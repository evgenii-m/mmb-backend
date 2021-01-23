package ru.pushkin.mmb.mapper;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import ru.pushkin.mmb.data.enumeration.PlaylistType;
import ru.pushkin.mmb.data.model.library.PlaylistData;
import ru.pushkin.mmb.data.model.library.PlaylistTrack;
import ru.pushkin.mmb.data.model.library.TrackData;
import ru.pushkin.mmb.deezer.model.Playlist;
import ru.pushkin.mmb.deezer.model.Track;
import ru.pushkin.mmb.utils.DateTimeUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Component
public class PlaylistDataMapper {
    private final TrackDataMapper trackDataMapper;

    public PlaylistData map(Playlist source, String userId) {
        LocalDateTime now = LocalDateTime.now();

        PlaylistData result = new PlaylistData();
        result.setTitle(source.getTitle());
        result.setDescription(source.getDescription());
        result.setActive(true);
        result.setType(PlaylistType.DEEZER);
        result.setSourceUrl(source.getLink());
        result.setUserId(userId);

        LocalDateTime createTime = now;
        List<PlaylistTrack> playlistTracks = new ArrayList<>();
        if (source.getTracks() != null && !CollectionUtils.isEmpty(source.getTracks().getData())) {
            createTime = source.getTracks().getData().stream()
                            .min(Comparator.comparing(Track::getTime_add))
                            .map(Track::getTime_add)
                            .map(date -> DateTimeUtils.toLocalDateTime(date.getTime()))
                            .orElse(now);
            for (int i = 0; i < source.getTracks().getData().size(); i++) {
                Track track = source.getTracks().getData().get(i);
                TrackData trackData = trackDataMapper.map(track);
                PlaylistTrack playlistTrack = new PlaylistTrack();
                playlistTrack.setPlaylistData(result);
                playlistTrack.setTrackData(trackData);
                playlistTrack.setPosition(i);
                Optional.ofNullable(track.getTime_add())
                        .ifPresent(date -> playlistTrack.setAdded(DateTimeUtils.toLocalDateTime(date.getTime())));
                playlistTracks.add(playlistTrack);
            }
        }
        result.setCreationTime(createTime);
        result.setTracks(playlistTracks);

        return result;
    }
}
