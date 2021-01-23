package ru.pushkin.mmb.api.output;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.pushkin.mmb.api.output.enumeration.PlaylistTypeParam;
import ru.pushkin.mmb.api.output.response.FavoriteTracksResponse;
import ru.pushkin.mmb.api.output.response.ListeningHistoryResponse;
import ru.pushkin.mmb.library.LibraryService;

import java.time.LocalDateTime;


@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/library")
public class LibraryController {

    private final LibraryService libraryService;


    @GetMapping(value = "/favorites", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<FavoriteTracksResponse> getFavoriteTracks(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "50") int size
    ) {
        log.debug("getFavoriteTracks (page: {}, size: {})", page, size);
        FavoriteTracksResponse favoriteTracks = libraryService.findFavoriteTracks(page, size);
        return ResponseEntity.ok(favoriteTracks);
    }

    @GetMapping(value = "/playlists", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity getPlaylists(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "50") int size,
            @RequestParam(name = "type", required = false) PlaylistTypeParam playlistType
    ) {
        log.debug("getListeningHistory (page: {}, size: {}, playlistType: {})", page, size, playlistType);
        return ResponseEntity.ok("Success");
    }

    @PostMapping(value = "/playlists/deezer")
    public ResponseEntity loadPlaylistsFromDeezer() {
        log.debug("loadPlaylistsFromDeezer");
        int playlistsCount = libraryService.loadPlaylistsForUserFromDeezer();
        return ResponseEntity.ok("Fetched data size = " + playlistsCount);
    }

    @GetMapping(value = "/history", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ListeningHistoryResponse> getListeningHistory(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "50") int size,
            @RequestParam(name = "from", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam(name = "to", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to
    ) {
        log.debug("getListeningHistory (page: {}, size: {}, from: {}, to: {})", page, size, from, to);
        return ResponseEntity.ok(libraryService.getUserListeningHistory(page, size, from, to));
    }

    @PostMapping(value = "/history")
    public ResponseEntity<String> loadListeningHistoryFromLastFm(
            @RequestParam(name = "userId") String userId,
            @RequestParam(name = "from")
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam(name = "to")
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to
    ) {
        log.debug("loadListeningHistoryFromLastFm (userId: {}, from: {}, to: {})", userId, from, to);
        long totalSize = libraryService.loadTrackDataForUserListeningHistory(userId, from, to);
        return ResponseEntity.ok("Fetched data size = " + totalSize);
    }

}
