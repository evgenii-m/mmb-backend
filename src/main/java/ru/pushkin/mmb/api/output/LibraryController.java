package ru.pushkin.mmb.api.output;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.pushkin.mmb.api.output.enumeration.PlaylistsFilterParam;
import ru.pushkin.mmb.api.output.response.FavoriteTracksResponse;
import ru.pushkin.mmb.api.output.response.ListeningHistoryResponse;
import ru.pushkin.mmb.api.output.response.PlaylistListResponse;
import ru.pushkin.mmb.api.output.dto.PlaylistDto;
import ru.pushkin.mmb.api.output.response.PlaylistResponse;
import ru.pushkin.mmb.exception.PlaylistNotFoundException;
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
        FavoriteTracksResponse response = libraryService.findFavoriteTracks(page, size);
        return ResponseEntity.ok(response);
    }

    @GetMapping(value = "/playlists", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<PlaylistListResponse> getPlaylistList(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "50") int size,
            @RequestParam(name = "filter", required = false, defaultValue = "ALL") PlaylistsFilterParam filter
    ) {
        log.debug("getPlaylistList (page: {}, size: {}, filter: {})", page, size, filter);
        PlaylistListResponse response = libraryService.getPlaylistList(page, size, filter);
        return ResponseEntity.ok(response);
    }

    @GetMapping(value = "/playlists/{playlistId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<PlaylistResponse> getPlaylist(@PathVariable(name = "playlistId") int playlistId) {
        log.debug("getPlaylist (playlistId: {})", playlistId);
        try {
            PlaylistResponse response = libraryService.getPlaylist(playlistId);
            return ResponseEntity.ok(response);
        } catch (PlaylistNotFoundException e) {
            log.error("getPlaylist error", e);
            return ResponseEntity.notFound().build();
        }
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
