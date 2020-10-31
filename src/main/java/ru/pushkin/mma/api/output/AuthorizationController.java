package ru.pushkin.mma.api.output;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.pushkin.mma.deezer.DeezerApiService;

@RestController
public class AuthorizationController {

    @Autowired
    private DeezerApiService deezerApiService;

    @GetMapping("/auth/deezer")
    public ResponseEntity<String> getDeezerAuthPageUrl() {
        return ResponseEntity.ok(deezerApiService.getUserAuthorizationPageUrl());
    }
}
