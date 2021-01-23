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
}
