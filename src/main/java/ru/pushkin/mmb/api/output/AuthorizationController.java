package ru.pushkin.mmb.api.output;

import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.pushkin.mmb.api.output.request.AuthRequest;
import ru.pushkin.mmb.api.output.request.RegistrationRequest;
import ru.pushkin.mmb.api.output.response.AuthResponse;
import ru.pushkin.mmb.data.model.User;
import ru.pushkin.mmb.deezer.DeezerApiErrorException;
import ru.pushkin.mmb.deezer.DeezerApiService;
import ru.pushkin.mmb.security.JwtTokenProvider;
import ru.pushkin.mmb.security.SecurityHelper;
import ru.pushkin.mmb.security.UserService;

import javax.validation.Valid;
import java.util.Optional;

@Log
@RequiredArgsConstructor
@RestController
public class AuthorizationController {

    private final UserService userService;
    private final JwtTokenProvider jwtTokenProvider;
    private final DeezerApiService deezerApiService;


    @PostMapping(value = "/register", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> registerUser(@RequestBody @Valid RegistrationRequest registrationRequest) {
        User user = userService.saveUser(registrationRequest.getLogin(), registrationRequest.getPassword());
        return ResponseEntity.ok(user.getId());
    }

    @PostMapping(value = "/auth", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<AuthResponse> auth(@RequestBody AuthRequest request) {
        Optional<User> user = userService.findByLoginAndPassword(request.getLogin(), request.getPassword());
        if (user.isPresent()) {
            String token = jwtTokenProvider.generateUserToken(user.get().getLogin());
            return ResponseEntity.ok(new AuthResponse(user.get().getId(), token));
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/auth/deezer")
    public ResponseEntity<String> getDeezerAuthPageUrl() {
        return ResponseEntity.ok(deezerApiService.getUserAuthorizationPageUrl());
    }


    @GetMapping("/auth/deezer/token/new")
    public ResponseEntity<String> authDeezerByToken(@RequestParam String code) {
        try {
            String accessToken = deezerApiService.obtainNewAccessToken(code);
            return ResponseEntity.ok(accessToken);
        } catch (DeezerApiErrorException e) {
            log.severe(e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    @GetMapping("/auth/deezer/token")
    public ResponseEntity<String> getDeezerAuthToken() {
        try {
            String accessToken = deezerApiService.getAccessToken();
            return ResponseEntity.ok(accessToken);
        } catch (DeezerApiErrorException e) {
            log.severe(e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
