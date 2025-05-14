package com.mindary.aichat.repositories;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import com.mindary.aichat.models.DiaryAnalysis;

public interface DiaryAnalysisRepository extends MongoRepository<DiaryAnalysis, String> {

    @Query(value = "{ 'senderId': ?0 }", sort = "{ 'createdAt': -1 }")
    List<DiaryAnalysis> findLatestBySenderId(String senderId, int limit);
}
