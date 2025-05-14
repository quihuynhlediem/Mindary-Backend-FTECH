package com.mindary.diary.services;

import com.mindary.diary.models.DiaryEntity;
import com.mindary.diary.models.DiaryImage;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;

public interface DiaryImageService {
    DiaryImage save(DiaryImage diaryImage);

    Set<DiaryImage> uploadAndSaveImages(List<MultipartFile> photos, DiaryEntity savedDiary);

    ArrayList<DiaryImage> findImageByDiaryId(UUID diaryId);

    String generatePresignedUrl(String s3Key);
}
