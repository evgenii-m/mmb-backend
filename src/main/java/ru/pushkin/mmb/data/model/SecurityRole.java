package ru.pushkin.mmb.data.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Getter
@Setter
@NoArgsConstructor
@Document("SecurityRole")
public class SecurityRole {
    @Id
    private String id;
    private String name;

    public SecurityRole(String name) {
        this.name = name;
    }
}