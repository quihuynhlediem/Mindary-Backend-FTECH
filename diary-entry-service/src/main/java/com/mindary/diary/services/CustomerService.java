package com.mindary.diary.services;

import java.util.UUID;

public interface CustomerService {
    String getPublicKey(UUID userId);
}
