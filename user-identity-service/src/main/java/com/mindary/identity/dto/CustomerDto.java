package com.mindary.identity.dto;

import jakarta.persistence.Column;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.LocalTime;

@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public class CustomerDto extends UserDto {
    private String username;
    private String firstName;
    private String lastName;
    private String age;
    private String gender;
    private String otp;
    private String encryptedPrivateKey;
    private String privateKeyIv;
    private int hour;
    private int minute;
    private String ampm;
    private LocalTime reminderTime;
}
