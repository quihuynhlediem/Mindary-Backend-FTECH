package com.mindary.identity.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AuthResponse {
    private String username;
    private String firstTimeLogin;
    private String accessToken;
    private String refreshToken;
    private UUID userId;
    private String salt;
}
