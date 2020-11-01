package ru.pushkin.mma.api.output;

import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import ru.pushkin.mma.api.output.request.AuthRequest;
import ru.pushkin.mma.api.output.request.RegistrationRequest;
import ru.pushkin.mma.api.output.response.AuthResponse;
import ru.pushkin.mma.data.model.User;
import ru.pushkin.mma.deezer.DeezerApiErrorException;
import ru.pushkin.mma.deezer.DeezerApiService;
import ru.pushkin.mma.security.JwtProvider;
import ru.pushkin.mma.security.SecurityHelper;
import ru.pushkin.mma.security.UserService;

import javax.validation.Valid;

@Log
@RestController
public class AuthorizationController {

    @Autowired
    private UserService userService;
    @Autowired
    private JwtProvider jwtProvider;

    @Autowired
    private DeezerApiService deezerApiService;


    @PostMapping(value = "/register", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> registerUser(@RequestBody @Valid RegistrationRequest registrationRequest) {
        User user = userService.saveUser(registrationRequest.getLogin(), registrationRequest.getPassword());
        return ResponseEntity.ok(user.getId());
    }

    @PostMapping(value = "/auth", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<AuthResponse> auth(@RequestBody AuthRequest request) {
        User user = userService.findByLoginAndPassword(request.getLogin(), request.getPassword());
        if (user == null) {
            return ResponseEntity.notFound().build();
        }
        String token = jwtProvider.generateToken(user.getLogin());
        return ResponseEntity.ok(new AuthResponse(user.getId(), token));
    }

    @GetMapping("/auth/deezer")
    public ResponseEntity<String> getDeezerAuthPageUrl() {
        return ResponseEntity.ok(deezerApiService.getUserAuthorizationPageUrl());
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

    @PostMapping("/auth/deezer/token")
    public ResponseEntity<String> authDeezerByToken(@RequestBody String code) {
        String accessToken = deezerApiService.obtainNewAccessToken(code);
        return ResponseEntity.ok(accessToken);
    }
}
