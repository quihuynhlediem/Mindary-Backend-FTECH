package com.mindary.aichat.repositories;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.mindary.aichat.models.UserDiaryInsight;

@Repository
public interface UserDiaryInsightRepository extends MongoRepository<UserDiaryInsight, String> {

    // Method to find the document by userId
    Optional<UserDiaryInsight> findByUserId(UUID userId);
}
