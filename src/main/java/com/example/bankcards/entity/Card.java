package com.example.bankcards.entity;

import com.example.bankcards.util.CardNumberEncryptor;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

/**
 * Сущность банковской карты.
 * <p>
 * Описывает основные атрибуты банковской карты, хранящиеся в базе данных.
 * Каждая карта привязана к пользователю ({@link User}) и имеет уникальный номер,
 * срок действия, статус, баланс и имя владельца.
 * <p>
 * Номер карты шифруется при сохранении в базу данных с использованием
 * {@link CardNumberEncryptor} для обеспечения безопасности персональных данных.
 * <p>
 * Статус карты ({@link CardStatus}) определяет её текущее состояние:
 * <ul>
 *   <li><b>ACTIVE</b> — карта активна и может использоваться для операций</li>
 *   <li><b>BLOCKED</b> — карта заблокирована (пользователем или администратором), операции запрещены</li>
 *   <li><b>EXPIRED</b> — срок действия карты истёк, автоматически переводится в этот статус</li>
 * </ul>
 * <p>
 * Переводы и списания средств разрешены только для карт со статусом {@code ACTIVE}.
 * Попытка операции с картой в статусе {@code BLOCKED} или {@code EXPIRED} приводит к ошибке.
 *
 * @see User
 * @see CardStatus
 * @see CardNumberEncryptor
 */
@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "cards")
public class Card {

    /**
     * Уникальный идентификатор карты.
     * Генерируется автоматически в базе данных с использованием стратегии IDENTITY.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Номер банковской карты.
     * <p>
     * Хранится в зашифрованном виде в базе данных с помощью {@link CardNumberEncryptor}.
     * Поле не отображается в API-документации ({@code hidden = true}), так как не возвращается напрямую.
     */
    @Schema(hidden = true)
    @Column(name = "number", nullable = false)
    @Convert(converter = CardNumberEncryptor.class)
    private String number;

    /**
     * ФИО владельца карты.
     * Отображается в интерфейсе и используется для идентификации карты.
     * Должно соответствовать данным в учётной записи пользователя.
     */
    @Column(name = "owner", nullable = false)
    private String owner;

    /**
     * Срок действия карты.
     * Формат: год-месяц-день (например, 2025-12-31).
     * После этой даты карта автоматически переводится в статус {@link CardStatus#EXPIRED}.
     */
    @Column(name = "expiration_date", nullable = false)
    private LocalDate expirationDate;

    /**
     * Текущий статус карты.
     * Определяет возможность проведения операций.
     * <p>
     * Возможные значения:
     * <ul>
     *   <li>{@link CardStatus#ACTIVE}</li>
     *   <li>{@link CardStatus#BLOCKED}</li>
     *   <li>{@link CardStatus#EXPIRED}</li>
     * </ul>
     */
    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    private CardStatus status;

    /**
     * Текущий баланс карты в рублях.
     * Должен быть неотрицательным числом.
     * Обновляется при переводах, пополнениях и списаниях.
     */
    @Column(name = "balance", nullable = false)
    private Double balance;

    /**
     * Пользователь, которому принадлежит карта.
     * <p>
     * Связь "многие к одному": несколько карт могут принадлежать одному пользователю.
     * Загрузка выполняется сразу (EAGER), чтобы избежать проблем с инициализацией
     * при доступе к данным пользователя.
     * Поле не отображается в API-документации ({@code hidden = true}).
     */
    @Schema(hidden = true)
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
}