package ru.pushkin.mmb.data.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Getter
@Setter
@NoArgsConstructor
@Document("SessionData")
public class SessionData {
    @Id
    private String id;
    private String code;
    private String value;
    private String userId;

    public SessionData(String code, String value, String userId) {
        this.code = code;
        this.value = value;
        this.userId = userId;
    }
}
