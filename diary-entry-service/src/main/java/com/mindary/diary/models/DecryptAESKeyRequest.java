package com.mindary.diary.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DecryptAESKeyRequest {
    String encryptedAESKey;
    String privateKey;
}
