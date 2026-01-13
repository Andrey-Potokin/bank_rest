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

/**
 * Конвертер для шифрования и дешифрования номеров банковских карт.
 * <p>
 * Реализует интерфейс {@link AttributeConverter} и используется JPA для автоматического
 * шифрования номера карты перед сохранением в базе данных и дешифрования при чтении.
 * Шифрование выполняется с использованием алгоритма AES.
 * <p>
 * Ключ шифрования задаётся через свойство {@code app.encryption.key} в файле конфигурации
 * (например, {@code application.yaml}). Длина ключа должна быть 16, 24 или 32 байта,
 * что соответствует AES-128, AES-192 или AES-256.
 */
@Converter
public class CardNumberEncryptor implements AttributeConverter<String, String> {

    /**
     * Секретный ключ для шифрования, загружаемый из конфигурации.
     * Должен быть длиной 16, 24 или 32 байта.
     */
    @Value("${app.encryption.key}")
    private String encryptionKey;

    /**
     * Объект ключа, используемый для операций шифрования и дешифрования.
     * Инициализируется после загрузки строки ключа.
     */
    private SecretKey secretKey;

    /**
     * Метод инициализации компонента после внедрения зависимостей.
     * <p>
     * Проверяет наличие и корректность длины ключа шифрования.
     * Создаёт объект {@link SecretKey} на основе строки ключа в кодировке UTF-8.
     *
     * @throws IllegalArgumentException если ключ не задан или имеет недопустимую длину
     */
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

    /**
     * Преобразует номер карты из сущности JPA в зашифрованное представление для хранения в базе данных.
     * <p>
     * Выполняет шифрование с помощью AES в режиме по умолчанию.
     * Результат кодируется в формат Base64 для безопасного хранения в текстовом поле.
     *
     * @param attribute номер карты в открытом виде; может быть null
     * @return зашифрованный номер карты в формате Base64; null, если атрибут равен null
     * @throws RuntimeException если произошла ошибка при шифровании
     */
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

    /**
     * Преобразует зашифрованный номер карты из базы данных в открытое представление для сущности JPA.
     * <p>
     * Декодирует строку из Base64 и выполняет дешифрование с помощью AES.
     * Результат преобразуется обратно в строку в кодировке UTF-8.
     *
     * @param dbData зашифрованный номер карты в формате Base64; может быть null
     * @return номер карты в открытом виде; null, если данные из БД равны null
     * @throws RuntimeException если произошла ошибка при дешифровании
     */
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