package ru.pushkin.mma.api.output;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import ru.pushkin.mma.api.output.request.AuthRequest;
import ru.pushkin.mma.api.output.request.RegistrationRequest;
import ru.pushkin.mma.api.output.response.AuthResponse;
import ru.pushkin.mma.data.model.User;
import ru.pushkin.mma.security.JwtProvider;
import ru.pushkin.mma.security.UserService;

import javax.validation.Valid;

@RestController
public class RegistrationController {
    @Autowired
    private UserService userService;
    @Autowired
    private JwtProvider jwtProvider;

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
}
