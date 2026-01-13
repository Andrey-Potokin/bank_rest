package com.example.bankcards.config;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.example.bankcards.exception.JwtValidationException;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Конфигурационный компонент для работы с JWT (JSON Web Token).
 * <p>
 * Обеспечивает:
 * <ul>
 *   <li>Генерацию JWT-токенов для аутентифицированных пользователей</li>
 *   <li>Валидацию входящих токенов</li>
 *   <li>Извлечение данных (логин, роли) из токена</li>
 * </ul>
 * <p>
 * Использует HMAC256 для подписи токена. Секретный ключ, время жизни и издатель
 * задаются через внешнюю конфигурацию ({@code application.yaml}).
 */
@Component
public class JwtConfig {

    /**
     * Секретный ключ для подписи JWT.
     * Должен быть не менее 32 символов для обеспечения безопасности.
     * Загружается из свойства {@code app.jwt.secret}.
     */
    @Value("${app.jwt.secret}")
    private String secretKey;

    /**
     * Время жизни JWT в миллисекундах.
     * Например: 86400000 = 24 часа.
     * Загружается из свойства {@code app.jwt.expiration}.
     */
    @Value("${app.jwt.expiration}")
    private long expirationTime;

    /**
     * Издатель (issuer) токена. Указывает, откуда выдан токен.
     * Используется при валидации для дополнительной проверки.
     * Загружается из свойства {@code app.jwt.issuer}.
     */
    @Value("${app.jwt.issuer}")
    private String issuer;

    /**
     * Алгоритм подписи JWT. Инициализируется после внедрения секретного ключа.
     */
    private Algorithm algorithm;

    /**
     * Метод инициализации компонента после создания бина.
     * Проверяет корректность секретного ключа и создаёт алгоритм HMAC256.
     *
     * @throws IllegalArgumentException если секретный ключ не задан или слишком короткий
     */
    @PostConstruct
    public void init() {
        if (secretKey == null || secretKey.length() < 32) {
            throw new IllegalArgumentException("JWT secret должен быть не менее 32 символов");
        }
        this.algorithm = Algorithm.HMAC256(secretKey);
    }

    /**
     * Генерирует JWT-токен на основе данных пользователя.
     * <p>
     * В токен включаются:
     * <ul>
     *   <li>Имя пользователя (subject)</li>
     *   <li>Издатель (issuer)</li>
     *   <li>Срок действия (expiration)</li>
     *   <li>Список ролей пользователя</li>
     * </ul>
     *
     * @param principal объект пользователя, реализующий {@link UserDetails}, не может быть null
     * @return строка с подписанным JWT-токеном
     * @throws IllegalArgumentException если переданный principal не является UserDetails
     */
    public String generateToken(Object principal) {
        if (!(principal instanceof UserDetails userDetails)) {
            throw new IllegalArgumentException(
                    "Principal должен реализовывать UserDetails. Получен: " + principal.getClass()
            );
        }

        return JWT.create()
                .withSubject(userDetails.getUsername())
                .withIssuer(issuer)
                .withExpiresAt(new Date(System.currentTimeMillis() + expirationTime))
                .withClaim("roles",
                        userDetails.getAuthorities().stream()
                                .map(GrantedAuthority::getAuthority)
                                .collect(Collectors.toList())
                )
                .sign(algorithm);
    }

    /**
     * Проверяет валидность JWT-токена.
     * <p>
     * Проверяет:
     * <ul>
     *   <li>Токен не пустой</li>
     *   <li>Целостность подписи</li>
     *   <li>Срок действия</li>
     *   <li>Издателя (issuer)</li>
     * </ul>
     *
     * @param token строка JWT-токена, не может быть null или пустой
     * @return декодированный JWT-объект {@link DecodedJWT}, если токен валиден
     * @throws JwtValidationException если токен недействителен или повреждён
     */
    public DecodedJWT validateToken(String token) {
        if (token == null || token.isEmpty()) {
            throw new JwtValidationException("Токен не может быть пустым");
        }

        try {
            return JWT.require(algorithm)
                    .withIssuer(issuer)
                    .build()
                    .verify(token);
        } catch (JWTVerificationException | IllegalArgumentException e) {
            throw new JwtValidationException("Невалидный JWT: " + e.getMessage());
        }
    }

    /**
     * Извлекает имя пользователя (логин) из JWT-токена.
     *
     * @param token строка JWT-токена
     * @return имя пользователя (subject из токена)
     * @throws JwtValidationException если токен недействителен
     */
    public String getUsernameFromToken(String token) {
        DecodedJWT decodedJWT = validateToken(token);
        return decodedJWT.getSubject();
    }

    /**
     * Извлекает список ролей пользователя из JWT-токена.
     * <p>
     * Если в токене нет поля "roles", возвращается пустой список.
     *
     * @param token строка JWT-токена
     * @return список строк с ролями, например: ["ROLE_USER", "ROLE_ADMIN"]
     * @throws JwtValidationException если токен недействителен
     */
    public List<String> getRolesFromToken(String token) {
        DecodedJWT decodedJWT = validateToken(token);

        if (decodedJWT.getClaim("roles").isNull()) {
            return Collections.emptyList();
        }

        return decodedJWT.getClaim("roles").asList(String.class);
    }
}