package ru.pushkin.mmb.api.output;

import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.pushkin.mmb.api.output.enumeration.PlaylistTypeParam;
import ru.pushkin.mmb.api.output.response.FavoriteTracksResponse;
import ru.pushkin.mmb.api.output.response.ListeningHistoryResponse;
import ru.pushkin.mmb.library.LibraryService;

import java.time.LocalDateTime;


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
        FavoriteTracksResponse favoriteTracks = libraryService.findFavoriteTracks(page, size);
        return ResponseEntity.ok(favoriteTracks);
    }

    @GetMapping(value = "/playlists", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity getPlaylists(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "50") int size,
            @RequestParam(name = "type", required = false) PlaylistTypeParam playlistTypeParam
    ) {
        return ResponseEntity.ok("Success");
    }

    @PostMapping(value = "/playlists/deezer")
    public ResponseEntity fetchPlaylistsFromDeezer() {
        libraryService.fetchPlaylistsForUserFromDeezer();
        return ResponseEntity.ok("Success");
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
        return ResponseEntity.ok(libraryService.getUserListeningHistory(page, size, from, to));
    }


}
