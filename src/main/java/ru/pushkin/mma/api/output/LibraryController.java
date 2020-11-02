package ru.pushkin.mma.api.output;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.pushkin.mma.api.output.response.FavoriteTracksResponse;
import ru.pushkin.mma.library.LibraryService;


@RestController
public class LibraryController {

    @Autowired
    private LibraryService libraryService;

    @GetMapping("/")
    public ResponseEntity status() {
        return ResponseEntity
                .ok("Status - OK");
    }

    @GetMapping(value = "/deezer/favorites", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<FavoriteTracksResponse> getFavoriteTracks(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "50") int size
    ) {
        FavoriteTracksResponse favoriteTracks = libraryService.findFavoriteTracks(page, size);
        return ResponseEntity.ok(favoriteTracks);
    }
}
