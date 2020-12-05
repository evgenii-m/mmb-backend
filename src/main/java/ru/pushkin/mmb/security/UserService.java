package ru.pushkin.mmb.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import ru.pushkin.mmb.data.enumeration.UserRoleCode;
import ru.pushkin.mmb.data.model.User;
import ru.pushkin.mmb.data.model.UserRole;
import ru.pushkin.mmb.data.repository.UserRepository;
import ru.pushkin.mmb.data.repository.UserRoleRepository;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private UserRoleRepository userRoleRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;

    public User saveUser(String login, String password) {
        User user = findByLogin(login);
        if (user != null) {
            throw new IllegalStateException("User already registered!");
        }
        UserRole userRole = userRoleRepository.findByName(UserRoleCode.ROLE_USER.getCode());
        String encodedPassword = passwordEncoder.encode(password);
        user = new User(login, encodedPassword, userRole);
        userRepository.save(user);
        return user;
    }

    public User findByLogin(String login) {
        return userRepository.findByLogin(login);
    }

    public User findByLoginAndPassword(String login, String password) {
        User user = findByLogin(login);
        if (user != null) {
            if (passwordEncoder.matches(password, user.getPassword())) {
                return user;
            }
        }
        return null;
    }
}
