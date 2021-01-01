package ru.pushkin.mmb.api.output;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
import ru.pushkin.mmb.lastfm.LastFmApiErrorException;
import ru.pushkin.mmb.lastfm.LastFmService;
import ru.pushkin.mmb.security.JwtTokenProvider;
import ru.pushkin.mmb.security.UserService;

import javax.validation.Valid;
import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
@RestController
public class AuthorizationController {

    private final UserService userService;
    private final JwtTokenProvider jwtTokenProvider;
    private final DeezerApiService deezerApiService;
    private final LastFmService lastFmService;


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


    @GetMapping("/auth/deezer/token")
    public ResponseEntity<String> getDeezerAccessToken(@RequestParam(required = false) String code) {
        return Utils.handleExceptions(() -> code != null ?
                ResponseEntity.ok(deezerApiService.obtainNewAccessToken(code)) :
                ResponseEntity.ok(deezerApiService.getAccessToken())
        );
    }


    @GetMapping("/auth/lastfm")
    public ResponseEntity<String> getLastFmAuthPageUrl() {
        return ResponseEntity.ok(lastFmService.formUserAuthorizationPageUrl());
    }

    @GetMapping("/auth/lastfm/session")
    public ResponseEntity<String> getLastFmSessionKey(
            @RequestParam(required = false) String token,
            @RequestParam(required = false) String username
    ) {
        return Utils.handleExceptions(() -> token != null ?
                ResponseEntity.ok(lastFmService.obtainNewSessionKey(token, username)) :
                ResponseEntity.ok(lastFmService.getSessionKey()));
    }

}
