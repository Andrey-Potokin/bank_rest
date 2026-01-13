package com.example.bankcards.service;

import com.example.bankcards.dto.CardCreateRequest;
import com.example.bankcards.dto.CardResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Интерфейс сервиса для управления банковскими картами.
 * <p>
 * Определяет методы для выполнения операций с банковскими картами, включая получение списка карт,
 * создание, блокировку, активацию, удаление, а также перевод средств между картами.
 * Реализация этого интерфейса обеспечивает бизнес-логику, связанную с обработкой данных карт.
 */
public interface CardService {

    /**
     * Получает страницу с банковскими картами, принадлежащими указанному пользователю.
     * <p>
     * Поддерживает пагинацию и сортировку с помощью объекта {@link Pageable}.
     * Возвращает объект {@link Page}, содержащий список {@link CardResponse} и метаданные пагинации.
     *
     * @param userId   идентификатор пользователя, чьи карты необходимо получить; не может быть null
     * @param pageable параметры пагинации: размер страницы, номер страницы, порядок сортировки
     * @return объект {@link Page} с данными о картах пользователя
     * @throws com.example.bankcards.exception.NotFoundException если пользователь с указанным ID не найден
     */
    Page<CardResponse> getUserCards(Long userId, Pageable pageable);

    /**
     * Получает страницу со всеми банковскими картами в системе.
     * <p>
     * Предназначен для использования администратором. Поддерживает пагинацию и сортировку.
     *
     * @param pageable параметры пагинации: размер страницы, номер страницы, порядок сортировки
     * @return объект {@link Page} со списком всех карт в системе
     */
    Page<CardResponse> getAllCards(Pageable pageable);

    /**
     * Создаёт новую банковскую карту для указанного пользователя.
     * <p>
     * Принимает DTO с данными карты и идентификатор пользователя, которому выдаётся карта.
     * После успешного создания возвращает данные созданной карты.
     *
     * @param request объект с данными для создания карты; не может быть null
     * @param userId  идентификатор пользователя, которому выдаётся карта; должен быть положительным
     * @return объект {@link CardResponse} с данными новой карты
     * @throws com.example.bankcards.exception.NotFoundException если пользователь с указанным ID не найден
     */
    CardResponse createCard(CardCreateRequest request, Long userId);

    /**
     * Блокирует указанную банковскую карту.
     * <p>
     * Устанавливает статус карты в {@link com.example.bankcards.entity.CardStatus#BLOCKED}.
     * Карта становится недоступной для любых операций.
     *
     * @param cardId идентификатор карты, которую необходимо заблокировать
     * @throws com.example.bankcards.exception.NotFoundException если карта с указанным ID не найдена
     * @throws com.example.bankcards.exception.TransferNotAllowedException если карта уже заблокирована или находится в недопустимом состоянии
     */
    void blockCard(Long cardId);

    /**
     * Активирует указанную банковскую карту.
     * <p>
     * Переводит карту из статуса {@link com.example.bankcards.entity.CardStatus#BLOCKED} в
     * {@link com.example.bankcards.entity.CardStatus#ACTIVE}, разрешая её использование.
     *
     * @param cardId идентификатор карты, которую необходимо активировать
     * @throws com.example.bankcards.exception.NotFoundException если карта с указанным ID не найдена
     * @throws com.example.bankcards.exception.TransferNotAllowedException если карта уже активна или заблокирована
     */
    void activateCard(Long cardId);

    /**
     * Удаляет банковскую карту из системы.
     * <p>
     * Операция безвозвратно удаляет карту по её идентификатору.
     * Может использоваться администратором для удаления ошибочно созданных карт.
     *
     * @param cardId идентификатор карты, которую необходимо удалить
     * @throws com.example.bankcards.exception.NotFoundException если карта с указанным ID не найдена
     */
    void deleteCard(Long cardId);

    /**
     * Выполняет перевод средств между двумя банковскими картами.
     * <p>
     * Проверяет:
     * <ul>
     *   <li>Принадлежность обеих карт одному пользователю</li>
     *   <li>Достаточность средств на карте-отправителе</li>
     *   <li>Активность обеих карт</li>
     *   <li>Корректность суммы перевода (положительная)</li>
     * </ul>
     * При успешной проверке списывает сумму с одной карты и зачисляет на другую.
     *
     * @param fromCardId идентификатор карты-отправителя
     * @param toCardId   идентификатор карты-получателя
     * @param amount     сумма перевода; должна быть положительной
     * @throws com.example.bankcards.exception.NotFoundException если одна из карт не найдена
     * @throws com.example.bankcards.exception.TransferNotAllowedException если перевод запрещён (например, карта заблокирована)
     * @throws com.example.bankcards.exception.InvalidAmountException если сумма перевода некорректна (отрицательная или нулевая)
     * @throws com.example.bankcards.exception.InsufficientFundsException если на карте-отправителе недостаточно средств
     */
    void transfer(Long fromCardId, Long toCardId, Double amount);
}