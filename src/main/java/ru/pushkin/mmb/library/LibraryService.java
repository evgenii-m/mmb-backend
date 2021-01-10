package ru.pushkin.mmb.library;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.pushkin.mmb.api.output.dto.TrackDto;
import ru.pushkin.mmb.api.output.response.FavoriteTracksResponse;
import ru.pushkin.mmb.api.output.response.ListeningHistoryResponse;
import ru.pushkin.mmb.config.ServicePropertyConfig;
import ru.pushkin.mmb.data.Pageable;
import ru.pushkin.mmb.data.model.library.TrackData;
import ru.pushkin.mmb.data.repository.TagDataRepository;
import ru.pushkin.mmb.data.repository.TrackDataRepository;
import ru.pushkin.mmb.data.repository.UserTrackInfoRepository;
import ru.pushkin.mmb.deezer.DeezerApiService;
import ru.pushkin.mmb.lastfm.LastFmService;
import ru.pushkin.mmb.lastfm.model.LovedTracks;
import ru.pushkin.mmb.mapper.TrackDataMapper;
import ru.pushkin.mmb.security.SecurityHelper;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Service
public class LibraryService {

    private final ServicePropertyConfig servicePropertyConfig;
    private final DeezerApiService deezerApiService;
    private final LastFmService lastFmService;
    private final TrackDataMapper trackDataMapper;
    private final TrackDataRepository trackDataRepository;
    private final TagDataRepository tagDataRepository;
    private final UserTrackInfoRepository userTrackInfoRepository;

    private ExecutorService executorService;


    @PostConstruct
    public void init() {
        executorService = Executors.newFixedThreadPool(servicePropertyConfig.getLastFm().getServiceThreadPoolSize());
    }

    @PreDestroy
    public void stop() {
        executorService.shutdown();
    }


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

    public ListeningHistoryResponse getUserListeningHistory(Integer page, Integer size, LocalDateTime from, LocalDateTime to) {
        Pageable<TrackData> response = fetchTrackDataForUserListeningHistory(
                SecurityHelper.getUserIdFromToken(), page, size, from, to
        );
        List<TrackDto> trackDtos = response.getData().stream()
                .map(trackDataMapper::map)
                .collect(Collectors.toList());
        return new ListeningHistoryResponse(page, size, response.getTotalSize(), trackDtos);
    }

    public Pageable<TrackData> fetchTrackDataForUserListeningHistory(
            @NotNull String userId, @NotNull Integer page, @NotNull Integer size,
            LocalDateTime from, LocalDateTime to
    ) {
        Date dateFrom = from != null ? Date.from(from.toInstant(ZoneOffset.UTC)) : null;
        Date dateTo = to != null ? Date.from(to.toInstant(ZoneOffset.UTC)) : null;
        Pageable<TrackData> recentTracks = lastFmService.fetchRecentTracks(userId, page, size, dateFrom, dateTo);
        List<TrackData> tracksData = fillTrackData(recentTracks.getData(), userId);
        return new Pageable<>(recentTracks.getPage(), tracksData.size(), recentTracks.getTotalPages(), recentTracks.getTotalSize(), tracksData);
    }

    private List<TrackData> fillTrackData(List<TrackData> tracks, String userId) {
        ConcurrentMap<String, TrackData> tracksStore = new ConcurrentHashMap<>(
                fetchTracksMapByMbidOrTitle(tracks, userId)
        );

        List<Callable<TrackData>> tasks = tracks.stream()
                .map(track -> (Callable<TrackData>) () -> {
                    TrackData storedTrack = tracksStore.get(track.getTitle());
                    if (storedTrack == null) {
                        log.debug("Begin fetch track data (track = {})", track);
                        TrackData trackData = lastFmService.fillTrackDataInfo(track, userId);

                        log.debug("End fetch track data (track = {})", track);
                        tracksStore.putIfAbsent(track.getTitle(), trackData);
                        return trackData;
                    }
                    return storedTrack;
                }).collect(Collectors.toList());

        List<TrackData> tracksData = new ArrayList<>();
        try {
            List<Future<TrackData>> futures = executorService.invokeAll(tasks);

            // wait until all task will be executed
            while (futures.stream().anyMatch(future -> !future.isDone() && !future.isCancelled())) {
                TimeUnit.MILLISECONDS.sleep(100);
            }

            for (Future<TrackData> future : futures) {
                try {
                    tracksData.add(future.get());
                } catch (InterruptedException | ExecutionException e) {
                    log.error("Executor Service error", e);
                }
            }

        } catch (InterruptedException e) {
            log.error("Executor Service error", e);
        }

        log.debug("Fetch tracks data end (tracks data size: {})", tracksData.size());
        return tracksData;
    }

    public long fetchTrackDataForUserListeningHistory(@NotNull String userId, @NotNull LocalDateTime from, @NotNull LocalDateTime to) {
        int pageSize = 200;

        log.debug("Start fetch listening history for user (userId: {}, from: {}, to: {}, page size: {})", userId, from, to, pageSize);

        long totalPages = 1;
        long totalSize = 0;
        for (int page = 0; page < totalPages; page++) {
            Pageable<TrackData> response = fetchTrackDataForUserListeningHistory(userId, page, pageSize, from, to);
            totalPages = response.getTotalPages();
            totalSize = response.getTotalSize();
            long responseSize = response.getSize();
            log.debug("Fetched page {} of {}, Page size = {}, Total size: {}", page, totalPages - 1, responseSize, totalSize);
            if (responseSize <= 0) {
                break;
            }
        }

        log.debug("Finish fetch listening history for user (userId: {}, from: {}, to: {}, total size: {}",
                userId, from, to, totalSize);

        return totalSize;
    }

    private Map<String, TrackData> fetchTracksMapByMbidOrTitle(List<TrackData> trackDatas, String userId) {
        List<String> mbids = trackDatas.stream()
                .map(TrackData::getMbid)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        List<String> titles = trackDatas.stream()
                .map(TrackData::getTitle)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        return trackDataRepository.findAllByMbidOrTitle(mbids, titles, userId).stream()
                .collect(Collectors.toMap(TrackData::getTitle, o -> o));
    }
}
