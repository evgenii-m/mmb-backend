package ru.pushkin.mmb.api.output;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.pushkin.mmb.data.enumeration.SecurityRoleCode;

@RestController
public class AdminController {

    @GetMapping("/")
    public ResponseEntity status() {
        return ResponseEntity
                .ok("Status - OK");
    }

    // TODO: add security check for ADMIN
    @PostMapping(value = "/history", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> loadUserDataFromLastFm() {
        return ResponseEntity
                .ok("Status - OK");
    }
}
