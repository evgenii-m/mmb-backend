package ru.pushkin.mmb.library;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.pushkin.mmb.api.output.dto.TrackDto;
import ru.pushkin.mmb.api.output.response.FavoriteTracksResponse;
import ru.pushkin.mmb.api.output.response.ListeningHistoryResponse;
import ru.pushkin.mmb.data.Pageable;
import ru.pushkin.mmb.data.model.library.HistoryTrackData;
import ru.pushkin.mmb.deezer.DeezerApiService;
import ru.pushkin.mmb.lastfm.LastFmService;
import ru.pushkin.mmb.lastfm.model.LovedTracks;
import ru.pushkin.mmb.mapper.TrackDataMapper;

import java.sql.Date;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class LibraryService {

    private final DeezerApiService deezerApiService;
    private final LastFmService lastFmService;
    private final TrackDataMapper trackDataMapper;

    public FavoriteTracksResponse findFavoriteTracks(Integer page, Integer size) {
//        List<Track> deezerFavoriteTracks = deezerApiService.getFavoriteTracks(page, size);
        Optional<LovedTracks> favoriteTracks = lastFmService.getFavoriteTracks(page, size);

        long totalSize = favoriteTracks.map(LovedTracks::getTotal).orElse(0L);
        List<TrackDto> trackDtos = favoriteTracks.map(LovedTracks::getTracks).stream()
                .flatMap(Collection::stream)
                .map(trackDataMapper::map)
                .collect(Collectors.toList());
        return new FavoriteTracksResponse(page, size, totalSize, trackDtos);
    }

    public ListeningHistoryResponse getListeningHistory(Integer page, Integer size, LocalDateTime from, LocalDateTime to) {
        Pageable<HistoryTrackData> response = lastFmService.fetchRecentTracks(
                page, size,
                from != null ? Date.from(from.toInstant(ZoneOffset.UTC)) : null,
                to != null ? Date.from(to.toInstant(ZoneOffset.UTC)) : null
        );
        List<TrackDto> trackDtos = response.getData().stream()
                .map(trackDataMapper::map)
                .collect(Collectors.toList());
        return new ListeningHistoryResponse(page, size, response.getTotalSize(), trackDtos);
    }
}
