package ru.pushkin.mmb.data.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;

@Getter
@NoArgsConstructor
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
