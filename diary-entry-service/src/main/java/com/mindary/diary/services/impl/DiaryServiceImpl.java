package com.mindary.diary.services.impl;

import com.mindary.diary.dto.AnalysisResultDto;
import com.mindary.diary.models.DiaryEntity;
import com.mindary.diary.repositories.DiaryRepository;
import com.mindary.diary.services.CustomerService;
import com.mindary.diary.services.DiaryService;
import com.mindary.diary.services.RabbitMQSender;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.keygen.KeyGenerators;
import org.springframework.stereotype.Service;

import java.io.UnsupportedEncodingException;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Base64;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

import javax.crypto.*;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

@Service
@RequiredArgsConstructor
@Slf4j
public class DiaryServiceImpl implements DiaryService {
    private final DiaryRepository diaryRepository;
    private final CustomerService customerService;
    private final Map<UUID, CompletableFuture<AnalysisResultDto>> pendingAnalysis = new ConcurrentHashMap<>();
    private final RabbitTemplate rabbitTemplate;
    private final RabbitMQSender rabbitMQSender;

    @Value(value = "${rabbitmq.queue.analysis_result.name:application.properties}")
    private String analysisResultQueue;

    @Value(value = "${rabbitmq.queue.analysis.name:application.properties}")
    private String analysisQueue;

    @Override
    public DiaryEntity create(UUID userId, String diary) throws NoSuchAlgorithmException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, InvalidKeyException, InvalidKeySpecException, InvalidAlgorithmParameterException, UnsupportedEncodingException {
        // Retrieve user's public key
        String publicKeyBase64 = getUserPublicKey(userId);
        PublicKey publicKey = decodePublicKey(publicKeyBase64);

        // Generate new AES key and IV
        byte[] aesKeyBytes = generateAESKeyBytes();
        SecretKeySpec aesKey = generateRandomAESKey(aesKeyBytes);
        byte[] ivBytes = generateIvBytes();
        String iv = generateIv(ivBytes);

        // Create AES cipher and encrypt diary content
        Cipher aesCipher = createAESCipher(aesKey, ivBytes);
        String encryptDiary = encryptDiary(aesCipher, diary);

        // Encrypt AES key with user's public key
        String encryptAESKey = encryptAESKey(aesKeyBytes, publicKey);

        // Saved the user's diary with encrypted content and encrypted AES Key
        DiaryEntity diaryEntity = DiaryEntity.builder()
                .userId(userId)
                .content(encryptDiary)
                .aesKey(encryptAESKey)
                .aesIv(iv)
                .build();

        return diaryRepository.save(diaryEntity);
    }

    @Override
    public AnalysisResultDto analyze (DiaryEntity savedDiary) {
        CompletableFuture<AnalysisResultDto> future = new CompletableFuture<>();
        pendingAnalysis.put(savedDiary.getId(), future);
        rabbitMQSender.sendDiary(savedDiary);

        return waitForAnalysisResult(savedDiary.getId());
    }

    private AnalysisResultDto waitForAnalysisResult(UUID diaryEntryId) {
        try {
            return pendingAnalysis.get(diaryEntryId).get();
        } catch (Exception e) {
            log.error("Error waiting for analysis result for diary entry ID: {}", diaryEntryId, e);
            throw new RuntimeException("Error waiting for analysis result", e);
        } finally {
            pendingAnalysis.remove(diaryEntryId);
        }
    }

    @RabbitListener(queues = "${rabbitmq.queue.analysis_result.name:application.properties}")
    public void receiveAnalysisResult(AnalysisResultDto analysisResult) {
        if (analysisResult == null) {
            log.warn("Received null analysis result.");
            return;
        }

        log.info(analysisResult.toString());
        log.info(analysisResult.getDiaryId().toString());

        CompletableFuture<AnalysisResultDto> future = pendingAnalysis.get(analysisResult.getDiaryId());

        if (future != null) {
            future.complete(analysisResult);
        } else {
            log.warn("No pending analysis found for diary ID: {}", analysisResult.getDiaryId());
        }
    }

    @Override
    public DiaryEntity save(DiaryEntity diary) {
        return diaryRepository.save(diary);
    }

    @Override
    public boolean isExist(UUID diaryId) {
        return diaryRepository.existsById(diaryId);
    }

    @Override
    public Optional<DiaryEntity> findOne(UUID diaryId) {
        return diaryRepository.findById(diaryId);
    }

    @Override
    public Page<DiaryEntity> findByUserId(UUID userId, Pageable pageable) {
        return diaryRepository.findByUserId(userId, pageable);
    }

    @Override
    public Optional<DiaryEntity> findByUserIdAndDate(UUID userId, String timezone) {
        ZoneId zone = ZoneId.of(timezone);
        LocalDate currentDate = LocalDate.now(zone);
        return diaryRepository.findByUserIdAndCreatedAtBetween(userId, currentDate.atStartOfDay(), currentDate.plusDays(1).atStartOfDay().minusNanos(1));
    }

    @Override
    public Optional<DiaryEntity> findByUserIdAndDate(UUID userId, LocalDate targetDate) {
        return diaryRepository.findByUserIdAndCreatedAtBetween(
                userId,
                targetDate.atStartOfDay(),
                targetDate.plusDays(1).atStartOfDay().minusNanos(1)
        );
    }

    @Override
    public DiaryEntity partialUpdate(UUID diaryId, DiaryEntity diaryEntity) {
        return diaryRepository.findById(diaryId).map(existingDiary -> {
            // Check if new content is provided
            if (diaryEntity.getContent() != null) {
                try {
                    // Retrieve user's public key
                    String publicKeyBase64 = getUserPublicKey(existingDiary.getUserId());
                    PublicKey publicKey = decodePublicKey(publicKeyBase64);

                    // Generate new AES key and IV
                    byte[] aesKeyBytes = generateAESKeyBytes();
                    SecretKeySpec aesKey = generateRandomAESKey(aesKeyBytes);
                    byte[] ivBytes = generateIvBytes();
                    String iv = generateIv(ivBytes);

                    // Create AES cipher and encrypt diary content
                    Cipher aesCipher = createAESCipher(aesKey, ivBytes);
                    String encryptDiary = encryptDiary(aesCipher, diaryEntity.getContent());

                    // Encrypt AES key with user's public key
                    String encryptAESKey = encryptAESKey(aesKeyBytes, publicKey);

                    // Update existing diary with new encrypted content, AES key, and IV
                    existingDiary.setContent(encryptDiary);
                    existingDiary.setAesKey(encryptAESKey);
                    existingDiary.setAesIv(iv);
                } catch (NoSuchAlgorithmException | NoSuchPaddingException | IllegalBlockSizeException |
                         BadPaddingException | InvalidKeyException | InvalidKeySpecException |
                         InvalidAlgorithmParameterException | UnsupportedEncodingException e) {
                    throw new RuntimeException("Failed to encrypt diary content: " + e.getMessage(), e);
                }
            }
            return diaryRepository.save(existingDiary);
        }).orElseThrow(() -> new RuntimeException("Diary does not exist"));
    }

    @Override
    public String decryptAesKey(String encryptedAesKeyBase64, String privateKeyBase64) throws Exception {
        // Import private key
        byte[] privateKeyBytes = Base64.getDecoder().decode(privateKeyBase64);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        PrivateKey privateKey = keyFactory.generatePrivate(new PKCS8EncodedKeySpec(privateKeyBytes));

        // Decrypt AES key (RSA-OAEP)
        byte[] encryptedAesKey = Base64.getDecoder().decode(encryptedAesKeyBase64);
        Cipher rsaCipher = Cipher.getInstance("RSA/ECB/OAEPWithSHA-256AndMGF1Padding");
        rsaCipher.init(Cipher.DECRYPT_MODE, privateKey);
        byte[] aesKeyBytes = rsaCipher.doFinal(encryptedAesKey);

        return Base64.getEncoder().encodeToString(aesKeyBytes);
    }

    private String getUserPublicKey(UUID userId) {
        return customerService.getPublicKey(userId);
    }

    private PublicKey decodePublicKey(String encodedPublicKey) throws NoSuchAlgorithmException, InvalidKeySpecException {
        byte[] publicKeyBytes = Base64.getDecoder().decode(encodedPublicKey);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        return keyFactory.generatePublic(new X509EncodedKeySpec(publicKeyBytes));
    }

    private byte[] generateAESKeyBytes() {
        SecureRandom random = new SecureRandom();
        byte[] aesKeyBytes = new byte[32]; // 256-bit
        random.nextBytes(aesKeyBytes);
        return aesKeyBytes;
    }
    private SecretKeySpec generateRandomAESKey(byte[] aesKeyBytes) throws NoSuchAlgorithmException {
        return new SecretKeySpec(aesKeyBytes, "AES");
    }

    private byte[] generateIvBytes() {
        SecureRandom random = new SecureRandom();
        byte[] ivBytes = new byte[12];
        random.nextBytes(ivBytes);
        return ivBytes;
    }

    private String generateIv(byte[] ivBytes) {
        return Base64.getEncoder().encodeToString(ivBytes);
    }

    private Cipher createAESCipher(SecretKeySpec aesKey, byte[] ivBytes) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, InvalidAlgorithmParameterException {
        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        cipher.init(Cipher.ENCRYPT_MODE, aesKey, new GCMParameterSpec(128, ivBytes));
        return cipher;
    }

    private String encryptDiary(Cipher cipher, String content) throws IllegalBlockSizeException, BadPaddingException, UnsupportedEncodingException {
        byte[] encryptedContent = cipher.doFinal(content.getBytes());
        return Base64.getEncoder().encodeToString(encryptedContent);
    }

    private String encryptAESKey(byte[] aesKeyBytes, PublicKey userPublicKey) throws NoSuchAlgorithmException, InvalidKeySpecException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
        Cipher rsaCipher = Cipher.getInstance("RSA/ECB/OAEPWithSHA-256AndMGF1Padding");
        rsaCipher.init(Cipher.ENCRYPT_MODE, userPublicKey);
        byte[] encryptedAesKey = rsaCipher.doFinal(aesKeyBytes);
        return Base64.getEncoder().encodeToString(encryptedAesKey);
    }
}
