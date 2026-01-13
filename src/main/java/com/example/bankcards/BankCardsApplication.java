package com.example.bankcards;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Основной класс приложения для системы управления банковскими картами.
 * <p>
 * Точка входа в приложение Spring Boot. Этот класс содержит метод {@code main},
 * с которого начинается выполнение программы. Аннотация {@link SpringBootApplication}
 * включает автоматическую настройку Spring Boot, сканирование компонентов
 * и конфигурацию на основе классов в пакете {@code com.example.bankcards} и подпакетах.
 * <p>
 * Приложение предоставляет REST API для:
 * <ul>
 *   <li>Аутентификации пользователей</li>
 *   <li>Управления банковскими картами (создание, блокировка, переводы)</li>
 *   <li>Работы с пользователями (создание, изменение ролей)</li>
 * </ul>
 */
@SpringBootApplication
public class BankCardsApplication {

    /**
     * Точка входа в приложение.
     * <p>
     * Запускает Spring Boot приложение. Метод загружает контекст Spring,
     * применяет автоматическую конфигурацию и инициализирует встроенный сервер (например, Tomcat).
     *
     * @param args аргументы командной строки, передаваемые при запуске (обычно не используются)
     */
    public static void main(String[] args) {
        SpringApplication.run(BankCardsApplication.class, args);
    }
}