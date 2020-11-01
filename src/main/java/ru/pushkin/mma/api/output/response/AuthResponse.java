package ru.pushkin.mma.api.output.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Getter
public class AuthResponse {
    private String userId;
    private String token;
}
