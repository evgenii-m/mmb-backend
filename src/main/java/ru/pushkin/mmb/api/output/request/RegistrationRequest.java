package ru.pushkin.mmb.api.output.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Getter
public class RegistrationRequest {
    private String login;
    private String password;

}
