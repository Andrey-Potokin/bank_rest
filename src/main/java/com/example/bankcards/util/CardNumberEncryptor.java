package com.example.bankcards.util;

import jakarta.annotation.PostConstruct;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.springframework.beans.factory.annotation.Value;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Converter
public class CardNumberEncryptor implements AttributeConverter<String, String> {

    @Value("${app.encryption.key}")
    private String encryptionKey;

    private SecretKey secretKey;

    @PostConstruct
    public void init() {
        if (encryptionKey == null || encryptionKey.isEmpty()) {
            throw new IllegalArgumentException(
                    "Ключ шифрования не настроен! Установите app.encryption.key в конфигурации"
            );
        }

        byte[] keyBytes = encryptionKey.getBytes(StandardCharsets.UTF_8);
        if (keyBytes.length != 16 && keyBytes.length != 24 && keyBytes.length != 32) {
            throw new IllegalArgumentException(
                    "Длина ключа должна быть 16, 24 или 32 байта (для AES-128/192/256)"
            );
        }

        this.secretKey = new SecretKeySpec(keyBytes, "AES");
    }

    @Override
    public String convertToDatabaseColumn(String attribute) {
        if (attribute == null) return null;
        try {
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);
            byte[] encrypted = cipher.doFinal(attribute.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(encrypted);
        } catch (Exception e) {
            throw new RuntimeException("Ошибка шифрования номера карты", e);
        }
    }

    @Override
    public String convertToEntityAttribute(String dbData) {
        if (dbData == null) return null;
        try {
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.DECRYPT_MODE, secretKey);
            byte[] decrypted = cipher.doFinal(Base64.getDecoder().decode(dbData));
            return new String(decrypted, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("Ошибка дешифрования номера карты", e);
        }
    }

}