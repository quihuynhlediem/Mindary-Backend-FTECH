package com.mindary.diary.services.impl;

import com.mindary.diary.models.DiaryEntity;
import com.mindary.diary.models.DiaryImage;
import com.mindary.diary.repositories.DiaryImageRepository;
import com.mindary.diary.services.DiaryImageService;
import com.mindary.diary.services.S3Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;

import java.net.URL;
import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class DiaryImageServiceImpl implements DiaryImageService {

    private final DiaryImageRepository diaryImageRepository;
    private final S3Service s3Service;
    private final S3Presigner s3Presigner;

    @Value(value = "${aws.s3.bucketname}")
    private String bucketName;

    @Override
    public DiaryImage save(DiaryImage diaryImage) {
        return diaryImageRepository.save(diaryImage);
    }

    @Override
    public Set<DiaryImage> uploadAndSaveImages(List<MultipartFile> images, DiaryEntity diaryEntity) {
        if (images == null || images.isEmpty()) {
            return Set.of();
        }

        return images.stream()
                .filter(image -> !image.isEmpty())
                .map(image -> {
                    try {
                        String s3Url = s3Service.uploadFile(image);
                        DiaryImage diaryImage = DiaryImage.builder()
                                .url(s3Url)
                                .diary(diaryEntity)
                                .build();
                        return diaryImageRepository.save(diaryImage);
                    } catch (Exception e) {
                        log.error("Error uploading or saving image: ", e);
                        throw new RuntimeException("Error processing images", e);
                    }
                })
                .collect(Collectors.toSet());
    }

    @Override
    public ArrayList<DiaryImage> findImageByDiaryId(UUID diaryId) {
        return diaryImageRepository.findDiaryImagesByDiary_Id(diaryId);
    }

    @Override
    public String generatePresignedUrl(String s3Key) {
        GetObjectPresignRequest getObjectPresignRequest = GetObjectPresignRequest.builder()
                .signatureDuration(Duration.ofMinutes(30))
                .getObjectRequest(builder -> builder.bucket(bucketName).key(s3Key))
                .build();
        PresignedGetObjectRequest presignedGetObjectRequest = s3Presigner.presignGetObject(getObjectPresignRequest);
        URL presignedUrl = presignedGetObjectRequest.url();
        return presignedUrl.toString();
    }
}
