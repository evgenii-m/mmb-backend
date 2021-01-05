package ru.pushkin.mmb.security;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import ru.pushkin.mmb.data.enumeration.SecurityRoleCode;
import ru.pushkin.mmb.data.model.UserData;
import ru.pushkin.mmb.data.model.SecurityRole;
import ru.pushkin.mmb.data.repository.UserDataRepository;
import ru.pushkin.mmb.data.repository.SecurityRoleRepository;

import java.util.Optional;

@RequiredArgsConstructor
@Service
public class UserService {

    private final UserDataRepository userDataRepository;
    private final SecurityRoleRepository securityRoleRepository;
    private final PasswordEncoder passwordEncoder;

    public UserData saveUser(String login, String password) {
        Optional<UserData> existedUserData = findByLogin(login);
        if (existedUserData.isPresent()) {
            throw new IllegalStateException("User already registered!");
        }
        SecurityRole securityRole = securityRoleRepository.findByName(SecurityRoleCode.USER.getCode()).orElseThrow();
        String encodedPassword = passwordEncoder.encode(password);
        return userDataRepository.save(
                new UserData(login, encodedPassword, securityRole)
        );
    }

    public Optional<UserData> findByLogin(String login) {
        return userDataRepository.findByLogin(login);
    }

    public UserData findByLoginAndPassword(String login, String password) {
        UserData userData = findByLogin(login).orElse(null);
        if (userData != null && passwordEncoder.matches(password, userData.getPassword())) {
            return userData;
        }
        return null;
    }
}
