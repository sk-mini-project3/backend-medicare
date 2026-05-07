package com.emr.medicare.common.util;

import com.emr.medicare.common.config.EncryptionProperties;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;

@Converter
@Component
@RequiredArgsConstructor
public class EncryptionConverter implements AttributeConverter<String, String> {

    private static final String ALGORITHM = "AES/CBC/PKCS5Padding";
    private static final int KEY_LENGTH = 32;
    private static final int IV_LENGTH = 16;

    private final EncryptionProperties encryptionProperties;

    @Override
    public String convertToDatabaseColumn(String attribute) {
        if (attribute == null) return null;
        try {
            // 암호화마다 새 IV 생성 → 동일 값도 매번 다른 암호문 생성 (레인보우 테이블 방지)
            byte[] iv = new byte[IV_LENGTH];
            new SecureRandom().nextBytes(iv);
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, buildKey(), new IvParameterSpec(iv));
            byte[] encrypted = cipher.doFinal(attribute.getBytes(StandardCharsets.UTF_8));
            // DB 저장 형식: Base64(IV):Base64(암호문) → 복호화 시 IV 복원에 필요
            return Base64.getEncoder().encodeToString(iv) + ":" + Base64.getEncoder().encodeToString(encrypted);
        } catch (Exception e) {
            throw new RuntimeException("Encryption failed", e);
        }
    }

    @Override
    public String convertToEntityAttribute(String dbData) {
        if (dbData == null) return null;
        try {
            String[] parts = dbData.split(":", 2);
            byte[] iv = Base64.getDecoder().decode(parts[0]);
            byte[] encrypted = Base64.getDecoder().decode(parts[1]);
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, buildKey(), new IvParameterSpec(iv));
            return new String(cipher.doFinal(encrypted), StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("Decryption failed", e);
        }
    }

    private SecretKeySpec buildKey() {
        byte[] raw = encryptionProperties.getSecretKey().getBytes(StandardCharsets.UTF_8);
        // AES-256은 정확히 32바이트 키 필요 → 환경변수 길이에 무관하게 패딩/자름 처리
        byte[] key = new byte[KEY_LENGTH];
        System.arraycopy(raw, 0, key, 0, Math.min(raw.length, KEY_LENGTH));
        return new SecretKeySpec(key, "AES");
    }
}
