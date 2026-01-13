package com.example.bankcards.service;

import com.example.bankcards.dto.UserCreateRequest;
import com.example.bankcards.dto.UserResponse;
import com.example.bankcards.entity.User;
import com.example.bankcards.entity.UserRole;
import com.example.bankcards.exception.NotFoundException;
import com.example.bankcards.repository.UserRepository;
import com.example.bankcards.util.UserUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

/**
 * Реализация сервиса для управления пользователями системы.
 * <p>
 * Содержит бизнес-логику для операций по созданию, получению, изменению роли и удалению пользователей.
 * Все операции выполняются с проверкой входных данных, уникальности логина и существования пользователя.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    /**
     * Репозиторий для работы с сущностями пользователей в базе данных.
     * Обеспечивает доступ к операциям CRUD и поиску по логину.
     */
    private final UserRepository userRepository;

    /**
     * Кодировщик паролей, используемый для безопасного хранения паролей.
     * Преобразует открытый пароль в хэш (например, с помощью BCrypt) перед сохранением.
     */
    private final PasswordEncoder passwordEncoder;

    /**
     * Создаёт нового пользователя в системе.
     * <p>
     * Выполняет проверку на уникальность логина. Если пользователь с таким логином уже существует,
     * выбрасывает исключение. В ином случае создаёт сущность пользователя, устанавливает зашифрованный
     * пароль и роль по умолчанию (USER), сохраняет в базе данных и возвращает DTO с данными.
     *
     * @param request  объект с данными пользователя (логин); не может быть null
     * @param password пароль пользователя в открытом виде; будет зашифрован перед сохранением
     * @return объект {@link UserResponse} с данными созданного пользователя (ID, логин, роли)
     * @throws IllegalArgumentException если логин уже занят
     */
    @Override
    @Transactional
    public UserResponse createUser(UserCreateRequest request, String password) {
        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            throw new IllegalArgumentException(
                    "Пользователь с логином '" + request.getUsername() + "' уже существует"
            );
        }

        User user = UserUtil.toEntity(request);
        user.setPassword(passwordEncoder.encode(password));
        user.setRoles(Set.of(UserRole.USER));

        User savedUser = userRepository.save(user);
        log.info("Создан пользователь ID={}, username={}", savedUser.getId(), savedUser.getUsername());
        return UserUtil.toDto(savedUser);
    }

    /**
     * Получает данные пользователя по его идентификатору.
     * <p>
     * Проверяет, что ID положительный. Ищет пользователя в базе данных по ID.
     * Если пользователь не найден — выбрасывает исключение. В противном случае преобразует
     * сущность в DTO и возвращает его.
     *
     * @param id идентификатор пользователя; должен быть положительным числом
     * @return объект {@link UserResponse} с логином, ID и ролями пользователя
     * @throws IllegalArgumentException если ID не положительный
     * @throws NotFoundException если пользователь с указанным ID не найден
     */
    @Override
    @Transactional(readOnly = true)
    public UserResponse getUserById(Long id) {
        if (id <= 0) {
            throw new IllegalArgumentException("ID пользователя должен быть положительным");
        }

        User user = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Пользователь с ID=" + id + " не найден"));
        return UserUtil.toDto(user);
    }

    /**
     * Обновляет (добавляет) роль пользователя.
     * <p>
     * Проверяет корректность ID и существование пользователя. Добавляет указанную роль
     * к текущему набору ролей пользователя. Если роль недопустима — выбрасывает исключение.
     * <p>
     * Примечание: в текущей реализации роль добавляется, но не заменяет существующие.
     * Возможны множественные роли.
     *
     * @param userId идентификатор пользователя; должен быть положительным
     * @param role   новая роль пользователя; не может быть null
     * @throws IllegalArgumentException если ID не положительный или роль недопустима
     * @throws NotFoundException если пользователь с указанным ID не найден
     */
    @Override
    @Transactional
    public void updateRole(Long userId, UserRole role) {
        if (userId <= 0) {
            throw new IllegalArgumentException("ID пользователя должен быть положительным");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с ID=" + userId + " не найден"));

        try {
            Set<UserRole> userRoles = user.getRoles();
            userRoles.add(role);
            user.setRoles(userRoles);
            userRepository.save(user);
            log.info("Роль пользователя ID={} обновлена/добавлена", userId);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Недопустимая роль: " + role);
        }
    }

    /**
     * Удаляет пользователя из системы по его идентификатору.
     * <p>
     * Проверяет, что ID положительный и пользователь существует. Если проверки пройдены —
     * удаляет запись из базы данных, включая связанные данные (например, карты).
     *
     * @param userId идентификатор пользователя; должен быть положительным
     * @throws IllegalArgumentException если ID не положительный
     * @throws NotFoundException если пользователь с указанным ID не найден
     */
    @Override
    @Transactional
    public void deleteUser(Long userId) {
        if (userId <= 0) {
            throw new IllegalArgumentException("ID пользователя должен быть положительным");
        }

        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("Пользователь с ID=" + userId + " не найден");
        }

        userRepository.deleteById(userId);
        log.info("Пользователь ID={} удалён", userId);
    }
}