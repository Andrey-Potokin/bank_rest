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

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

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