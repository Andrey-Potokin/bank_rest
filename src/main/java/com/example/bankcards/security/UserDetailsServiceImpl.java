package com.example.bankcards.security;

import com.example.bankcards.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * Реализация интерфейса {@link UserDetailsService} для загрузки данных пользователя по логину.
 * <p>
 * Этот сервис используется Spring Security для аутентификации пользователя.
 * При попытке входа в систему, Spring Security вызывает метод {@link #loadUserByUsername(String)},
 * передавая логин пользователя. Сервис ищет пользователя в базе данных с помощью {@link UserRepository}.
 * Если пользователь найден — возвращает объект {@link UserDetails}, иначе выбрасывает исключение.
 * <p>
 * Класс помечен аннотацией {@link Service}, чтобы быть автоматически обнаружен Spring-контейнером.
 * Использует внедрение зависимостей через конструктор (благодаря {@link RequiredArgsConstructor}).
 */
@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    /**
     * Репозиторий для работы с пользователями в базе данных.
     * Используется для поиска пользователя по логину.
     */
    private final UserRepository userRepository;

    /**
     * Загружает пользователя по его логину.
     * <p>
     * Выполняет поиск в базе данных с помощью метода {@link UserRepository#findByUsername(String)}.
     * Если пользователь не найден, выбрасывает {@link UsernameNotFoundException}.
     
     *
     * @param username логин пользователя (не может быть null)
     * @return объект {@link UserDetails}, содержащий логин, пароль и роли пользователя
     * @throws UsernameNotFoundException если пользователь с указанным логином не найден в базе данных
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException(
                        "Пользователь с логином '" + username + "' не найден"
                ));
    }
}