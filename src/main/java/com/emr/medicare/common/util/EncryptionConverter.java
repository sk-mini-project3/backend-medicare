package com.emr.medicare.common.util;

import com.emr.medicare.common.config.EncryptionProperties;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
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
        if (dbData == null) {
            return null;
        }
        String trimmed = dbData.trim();
        if (trimmed.isEmpty()) {
            return null;
        }
        // 저장 포맷은 반드시 "단일콜론"으로 IV와 암호문을 구분. 콜론이 없으면 레거시 평문(시드·하이픈 주민번호 등).
        if (!trimmed.contains(":")) {
            return trimmed;
        }
        String[] parts = trimmed.split(":", 2);
        if (parts.length != 2 || parts[0].isEmpty() || parts[1].isEmpty()) {
            return trimmed;
        }
        try {
            byte[] iv = Base64.getDecoder().decode(parts[0]);
            byte[] encrypted = Base64.getDecoder().decode(parts[1]);
            if (iv.length != IV_LENGTH) {
                return trimmed;
            }
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, buildKey(), new IvParameterSpec(iv));
            return new String(cipher.doFinal(encrypted), StandardCharsets.UTF_8);
        } catch (IllegalArgumentException e) {
            // Base64가 아니면 IV:암호문 포맷이 아님 → 평문 등으로 간주
            return trimmed;
        } catch (Exception e) {
            log.warn(
                    "resident_number 복호화 실패(키 변경·손상·암호문 불일치). 해당 필드는 null 처리: {}",
                    e.toString()
            );
            return null;
        }
    }

    private SecretKeySpec buildKey() {
        String sk = encryptionProperties.getSecretKey();
        byte[] raw = (sk == null ? "" : sk).getBytes(StandardCharsets.UTF_8);
        // AES-256은 정확히 32바이트 키 필요 → 환경변수 길이에 무관하게 패딩/자름 처리
        byte[] key = new byte[KEY_LENGTH];
        System.arraycopy(raw, 0, key, 0, Math.min(raw.length, KEY_LENGTH));
        return new SecretKeySpec(key, "AES");
    }
}
