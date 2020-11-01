package ru.pushkin.mma.data.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;

@Getter
@NoArgsConstructor
public class UserRole {
    @Id
    private String id;
    private String name;

    public UserRole(String name) {
        this.name = name;
    }
}
