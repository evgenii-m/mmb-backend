package ru.pushkin.mmb.api.output;

import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.pushkin.mmb.data.enumeration.SecurityRoleCode;
import ru.pushkin.mmb.library.LibraryService;

import java.time.LocalDateTime;

@RequiredArgsConstructor
@RestController
public class AdminController {

    private final LibraryService libraryService;


    @GetMapping("/")
    public ResponseEntity status() {
        return ResponseEntity
                .ok("Status - OK");
    }

    // TODO: add security check for ADMIN
    @PostMapping(value = "/history", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> fetchUserDataFromLastFm(
            @RequestParam(name = "userId") String userId,
            @RequestParam(name = "from")
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam(name = "to")
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to) {
        long totalSize = libraryService.fetchTrackDataForUserListeningHistory(userId, from, to);
        return ResponseEntity
                .ok("Status - OK, fetched data size = " + totalSize);
    }
}
