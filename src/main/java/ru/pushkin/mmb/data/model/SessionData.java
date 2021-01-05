package ru.pushkin.mmb.data.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "session_data")
public class SessionData {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @NotNull
    private String code;

    @NotNull
    private String value;

    @NotNull
    @Column(name = "user_id")
    private String userId;

    public SessionData(String code, String value, String userId) {
        this.code = code;
        this.value = value;
        this.userId = userId;
    }
}
