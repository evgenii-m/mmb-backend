package ru.pushkin.mma.api.output;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class LibraryController {

    @GetMapping("/")
    public ResponseEntity status() {
        return ResponseEntity
                .ok("Status - OK");
    }
}
