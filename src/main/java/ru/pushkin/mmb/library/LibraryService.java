package ru.pushkin.mmb.library;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.pushkin.mmb.api.output.dto.TrackDto;
import ru.pushkin.mmb.api.output.response.FavoriteTracksResponse;
import ru.pushkin.mmb.api.output.response.ListeningHistoryResponse;
import ru.pushkin.mmb.data.Pageable;
import ru.pushkin.mmb.data.model.library.TrackData;
import ru.pushkin.mmb.deezer.DeezerApiService;
import ru.pushkin.mmb.lastfm.LastFmService;
import ru.pushkin.mmb.lastfm.model.LovedTracks;
import ru.pushkin.mmb.mapper.TrackDataMapper;
import ru.pushkin.mmb.security.SecurityHelper;

import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Service
public class LibraryService {

    private final DeezerApiService deezerApiService;
    private final LastFmService lastFmService;
    private final TrackDataMapper trackDataMapper;

    public FavoriteTracksResponse findFavoriteTracks(Integer page, Integer size) {
        String userId = SecurityHelper.getUserIdFromToken();
//        List<Track> deezerFavoriteTracks = deezerApiService.getFavoriteTracks(page, size);
        Optional<LovedTracks> favoriteTracks = lastFmService.getFavoriteTracks(userId, page, size);

        long totalSize = favoriteTracks.map(LovedTracks::getTotal).orElse(0L);
        List<TrackDto> trackDtos = favoriteTracks.map(LovedTracks::getTracks).stream()
                .flatMap(Collection::stream)
                .map(trackDataMapper::map)
                .collect(Collectors.toList());
        return new FavoriteTracksResponse(page, size, totalSize, trackDtos);
    }

    public ListeningHistoryResponse getListeningHistory(Integer page, Integer size, LocalDateTime from, LocalDateTime to) {
        Pageable<TrackData> response = lastFmService.fetchRecentTracks(
                SecurityHelper.getUserIdFromToken(), page, size,
                from != null ? Date.from(from.toInstant(ZoneOffset.UTC)) : null,
                to != null ? Date.from(to.toInstant(ZoneOffset.UTC)) : null
        );
        List<TrackDto> trackDtos = response.getData().stream()
                .map(trackDataMapper::map)
                .collect(Collectors.toList());
        return new ListeningHistoryResponse(page, size, response.getTotalSize(), trackDtos);
    }


    public long fetchTrackDataForUserListeningHistory(@NotNull String userId, @NotNull LocalDateTime from, @NotNull LocalDateTime to) {
        int pageSize = 200;
        Date dateFrom = Date.from(from.toInstant(ZoneOffset.UTC));
        Date dateTo = Date.from(to.toInstant(ZoneOffset.UTC));

        log.info("Start fetch listening history for user (userId: {}, from: {}, to: {}, page size: {})", userId, from, to, pageSize);

        long totalPages = 1;
        long totalSize = 0;
        for (int page = 0; page < totalPages; page++) {
            Pageable<TrackData> response = lastFmService.fetchRecentTracks(userId, page, pageSize, dateFrom, dateTo);
            totalPages = response.getTotalPages();
            totalSize = response.getTotalSize();
            long responseSize = response.getSize();
            log.info("Fetched page {} of {}, Page size = {}, Total size: {}", page, totalPages - 1, responseSize, totalSize);
            if (responseSize <= 0) {
                break;
            }
        }

        log.info("Finish fetch listening history for user (userId: {}, from: {}, to: {}, total size: {}",
                userId, from, to, totalSize);

        return totalSize;
    }
}
