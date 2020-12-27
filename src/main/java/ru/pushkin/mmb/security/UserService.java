package ru.pushkin.mmb.security;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import ru.pushkin.mmb.data.enumeration.SecurityRoleCode;
import ru.pushkin.mmb.data.model.User;
import ru.pushkin.mmb.data.model.SecurityRole;
import ru.pushkin.mmb.data.repository.UserRepository;
import ru.pushkin.mmb.data.repository.SecurityRoleRepository;

import java.util.Optional;

@RequiredArgsConstructor
@Service
public class UserService {

    private final UserRepository userRepository;
    private final SecurityRoleRepository securityRoleRepository;
    private final PasswordEncoder passwordEncoder;

    public User saveUser(String login, String password) {
        Optional<User> existedUser = findByLogin(login);
        if (existedUser.isPresent()) {
            throw new IllegalStateException("User already registered!");
        }
        SecurityRole securityRole = securityRoleRepository.findByName(SecurityRoleCode.USER.getCode());
        String encodedPassword = passwordEncoder.encode(password);
        return userRepository.save(
                new User(login, encodedPassword, securityRole)
        );
    }

    public Optional<User> findByLogin(String login) {
        return userRepository.findByLogin(login);
    }

    public Optional<User> findByLoginAndPassword(String login, String password) {
        return findByLogin(login)
                .filter(user -> passwordEncoder.matches(password, user.getPassword()));
    }
}
