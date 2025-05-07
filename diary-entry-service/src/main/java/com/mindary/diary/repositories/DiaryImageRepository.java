package com.mindary.diary.repositories;

import com.mindary.diary.models.DiaryImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.UUID;

@Repository
public interface DiaryImageRepository extends JpaRepository<DiaryImage, UUID> {
    ArrayList<DiaryImage> findDiaryImagesByDiary_Id(UUID diaryId);
}
