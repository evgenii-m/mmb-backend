package ru.pushkin.mmb.data.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@Document("User")
public class User {
    @Id
    private String id;
    private String login;
    private String password;
    private Set<SecurityRole> roles;

    public User(String login, String password, SecurityRole role) {
        this.login = login;
        this.password = password;
        this.roles = Set.of(role);
    }
}
