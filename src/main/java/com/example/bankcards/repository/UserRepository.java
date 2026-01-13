package com.example.bankcards.repository;

import com.example.bankcards.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Репозиторий для управления сущностями {@link User} (пользователи системы).
 * <p>
 * Предоставляет методы для выполнения стандартных операций CRUD (создание, чтение, обновление, удаление)
 * с использованием Spring Data JPA. Наследует все базовые операции от {@link JpaRepository}.
 * <p>
 * Дополнительно содержит пользовательский метод для поиска пользователя по логину,
 * что необходимо для аутентификации и проверки уникальности учётной записи.
 
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Находит пользователя по его логину.
     * <p>
     * Используется при аутентификации и регистрации для проверки существования пользователя.
     * Возвращает пустой {@link Optional}, если пользователь с таким логином не найден.
     *
     * @param username логин пользователя, который необходимо найти; не должен быть null
     * @return объект {@link Optional} с найденным пользователем или пустым значением, если пользователь не существует
     */
    Optional<User> findByUsername(String username);
}