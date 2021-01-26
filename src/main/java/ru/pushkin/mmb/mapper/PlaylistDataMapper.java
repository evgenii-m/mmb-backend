package ru.pushkin.mmb.mapper;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import ru.pushkin.mmb.api.output.dto.PlaylistDto;
import ru.pushkin.mmb.api.output.dto.PlaylistShortDto;
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
import java.util.stream.Collectors;

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

    public PlaylistShortDto mapShort(PlaylistData source) {
        return PlaylistShortDto.builder()
                .id(source.getId())
                .title(source.getTitle())
                .description(source.getDescription())
                .creationTime(source.getCreationTime())
                .active(source.isActive())
                .sync(source.isSync())
                .type(source.getType())
                .sourceUrl(source.getSourceUrl())
                .tracksCount(source.getTracks().size())
                .build();
    }

    public PlaylistDto map(PlaylistData source) {
        PlaylistDto result = new PlaylistDto();
        result.setId(source.getId());
        result.setTitle(source.getTitle());
        result.setDescription(source.getDescription());
        result.setCreationTime(source.getCreationTime());
        result.setActive(source.isActive());
        result.setSync(source.isSync());
        result.setType(source.getType());
        result.setSourceUrl(source.getSourceUrl());
        result.setTracksCount(source.getTracks().size());
        result.setTracks(
            source.getTracks().stream()
                    .sorted(Comparator.comparing(PlaylistTrack::getPosition))
                    .map(trackDataMapper::map)
                    .collect(Collectors.toList())
        );
        return result;
    }
}
