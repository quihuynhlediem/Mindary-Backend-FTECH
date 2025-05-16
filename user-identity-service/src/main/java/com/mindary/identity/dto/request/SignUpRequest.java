package com.mindary.identity.dto.request;

import com.mindary.identity.models.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SignUpRequest {
    private String firstName;
    private String lastName;
    private String username;
    private String password;
    private String email;
    private String publicKey;
    private String salt;
    private String encryptedPrivateKey;
    private String privateKeyIv;
    private User.UserRole role;
}