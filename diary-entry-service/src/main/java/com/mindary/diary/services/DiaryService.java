package com.mindary.diary.services;

import com.mindary.diary.dto.AnalysisResultDto;
import com.mindary.diary.models.DiaryEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

@Service
public interface DiaryService {

    DiaryEntity create(UUID userId, String diary) throws NoSuchAlgorithmException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, InvalidKeyException, InvalidKeySpecException, InvalidAlgorithmParameterException, UnsupportedEncodingException;

    AnalysisResultDto analyze (DiaryEntity savedDiary);

    DiaryEntity save(DiaryEntity diary);

    boolean isExist(UUID diaryId);

    Optional<DiaryEntity> findOne(UUID diaryId);

    DiaryEntity partialUpdate(UUID diaryId,DiaryEntity diaryEntity);

    Page<DiaryEntity> findByUserId(UUID userId, Pageable pageable);

    Optional<DiaryEntity> findByUserIdAndDate(UUID userId, String timezone);

    Optional<DiaryEntity> findByUserIdAndDate(UUID userId, LocalDate targetDate);

    String decryptAesKey(String encryptedAesKeyBase64, String privateKeyBase64) throws Exception;
}
