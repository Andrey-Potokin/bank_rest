package com.example.bankcards.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Base64;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CardNumberEncryptorTest {

    private final CardNumberEncryptor encryptor = new CardNumberEncryptor();

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(encryptor, "encryptionKey", "0123456789ABCDEF");
        encryptor.init();
    }

    @Test
    void testEncryptAndDecrypt_ShouldReturnOriginalValue() {
        String original = "4111111111111111";
        String encrypted = encryptor.convertToDatabaseColumn(original);
        assertThat(encrypted).isNotNull().isNotEqualTo(original);
        String decrypted = encryptor.convertToEntityAttribute(encrypted);
        assertThat(decrypted).isEqualTo(original);
    }

    @Test
    void testConvertToDatabaseColumn_NullInput_ShouldReturnNull() {
        String result = encryptor.convertToDatabaseColumn(null);
        assertThat(result).isNull();
    }

    @Test
    void testConvertToEntityAttribute_NullInput_ShouldReturnNull() {
        String result = encryptor.convertToEntityAttribute(null);
        assertThat(result).isNull();
    }

    @Test
    void testInit_WithShortKey_ShouldThrowException() {
        ReflectionTestUtils.setField(encryptor, "encryptionKey", "short");
        assertThrows(IllegalArgumentException.class, () -> encryptor.init(),
                "Длина ключа должна быть 16, 24 или 32 байта (для AES-128/192/256)");
    }

    @Test
    void testInit_WithLongKey_ShouldThrowException() {
        ReflectionTestUtils.setField(encryptor, "encryptionKey",
                "0123456789ABCDEF0123456789ABCDEF01");
        assertThrows(IllegalArgumentException.class, () -> encryptor.init(),
                "Длина ключа должна быть 16, 24 или 32 байта (для AES-128/192/256)");
    }

    @Test
    void testInit_WithEmptyKey_ShouldThrowException() {
        ReflectionTestUtils.setField(encryptor, "encryptionKey", "");
        assertThrows(IllegalArgumentException.class, () -> encryptor.init(),
                "Ключ шифрования не настроен! Установите app.encryption.key в конфигурации");
    }

    @Test
    void testInit_WithNullKey_ShouldThrowException() {
        ReflectionTestUtils.setField(encryptor, "encryptionKey", null);
        assertThrows(IllegalArgumentException.class, () -> encryptor.init(),
                "Ключ шифрования не настроен! Установите app.encryption.key в конфигурации");
    }

    @Test
    void testConvertToDatabaseColumn_WhenEncryptionFails_ShouldThrowRuntimeException() {
        ReflectionTestUtils.setField(encryptor, "secretKey", null);
        assertThrows(RuntimeException.class, () -> encryptor.convertToDatabaseColumn("1234"),
                "Ошибка шифрования номера карты");
    }

    @Test
    void testConvertToEntityAttribute_WhenDecryptionFails_ShouldThrowRuntimeException() {
        assertThrows(RuntimeException.class, () -> encryptor.convertToEntityAttribute("not-base64"),
                "Ошибка дешифрования номера карты");

        String invalidBase64 = Base64.getEncoder().encodeToString(new byte[]{0x00, 0x01, 0x02});
        assertThrows(RuntimeException.class, () -> encryptor.convertToEntityAttribute(invalidBase64 + "XXX"),
                "Ошибка дешифрования номера карты");
    }

    @Test
    void testInit_With16ByteKey_ShouldSucceed() {
        ReflectionTestUtils.setField(encryptor, "encryptionKey", "0123456789ABCDEF");
        encryptor.init();
    }

    @Test
    void testInit_With24ByteKey_ShouldSucceed() {
        ReflectionTestUtils.setField(encryptor, "encryptionKey", "0123456789ABCDEF01234567");
        encryptor.init();
    }

    @Test
    void testInit_With32ByteKey_ShouldSucceed() {
        ReflectionTestUtils.setField(encryptor, "encryptionKey", "0123456789ABCDEF0123456789ABCDEF");
        encryptor.init();
    }
}