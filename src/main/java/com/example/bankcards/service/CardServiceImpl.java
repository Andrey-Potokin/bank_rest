package com.example.bankcards.service;

import com.example.bankcards.dto.CardCreateRequest;
import com.example.bankcards.dto.CardResponse;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.CardStatus;
import com.example.bankcards.entity.User;
import com.example.bankcards.exception.InsufficientFundsException;
import com.example.bankcards.exception.InvalidAmountException;
import com.example.bankcards.exception.NotFoundException;
import com.example.bankcards.exception.TransferNotAllowedException;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.repository.UserRepository;
import com.example.bankcards.util.CardUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Реализация сервиса для управления банковскими картами.
 * <p>
 * Предоставляет бизнес-логику для операций с картами: получение списка, создание, блокировка,
 * активация, удаление и перевод средств между картами. Все операции выполняются с учётом
 * прав доступа и состояния карт.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CardServiceImpl implements CardService {

    /**
     * Репозиторий для работы с сущностями банковских карт.
     * Обеспечивает доступ к данным карт в базе данных.
     */
    private final CardRepository cardRepository;

    /**
     * Репозиторий для работы с сущностями пользователей.
     * Используется для проверки существования пользователя при создании карты
     * и для контроля доступа к операциям.
     */
    private final UserRepository userRepository;

    /**
     * Получает страницу с банковскими картами указанного пользователя.
     * <p>
     * Проверяет, что идентификатор пользователя положителен и пользователь существует.
     * Возвращает карты с поддержкой пагинации (размер страницы, номер, сортировка).
     *
     * @param userId   идентификатор пользователя, чьи карты запрашиваются; должен быть положительным
     * @param pageable параметры пагинации: размер, номер страницы, направление сортировки
     * @return объект {@link Page} с данными о картах в виде {@link CardResponse}
     * @throws IllegalArgumentException если ID пользователя не положительный
     * @throws NotFoundException если пользователь с указанным ID не найден
     */
    @Override
    @Transactional(readOnly = true)
    public Page<CardResponse> getUserCards(Long userId, Pageable pageable) {
        if (userId <= 0) {
            throw new IllegalArgumentException("ID пользователя должен быть положительным");
        }

        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("Пользователь с ID=" + userId + " не найден");
        }

        Page<Card> cardsPage = cardRepository.findByUserId(userId, pageable);
        List<CardResponse> cardDtos = cardsPage.getContent().stream()
                .map(CardUtil::toDto)
                .toList();

        return new PageImpl<>(cardDtos, pageable, cardsPage.getTotalElements());
    }

    /**
     * Получает страницу со всеми банковскими картами в системе.
     * <p>
     * Предназначен для использования администратором. Возвращает все карты
     * с поддержкой пагинации и сортировки.
     *
     * @param pageable параметры пагинации: размер, номер страницы, направление сортировки
     * @return объект {@link Page} со списком всех карт в виде {@link CardResponse}
     */
    @Override
    @Transactional(readOnly = true)
    public Page<CardResponse> getAllCards(Pageable pageable) {
        return cardRepository.findAll(pageable)
                .map(CardUtil::toDto);
    }

    /**
     * Создаёт новую банковскую карту для указанного пользователя.
     * <p>
     * Проверяет существование пользователя по ID. Создаёт карту с переданными параметрами,
     * устанавливает статус "ACTIVE" и сохраняет в базе данных. Возвращает данные созданной карты.
     *
     * @param request объект с данными для создания карты; не может быть null
     * @param userId  идентификатор пользователя, которому выдаётся карта
     * @return объект {@link CardResponse} с данными новой карты
     * @throws NotFoundException если пользователь с указанным ID не найден
     */
    @Override
    @Transactional
    public CardResponse createCard(CardCreateRequest request, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с ID=" + userId + " не найден"));

        Card card = CardUtil.toEntity(request);
        card.setStatus(CardStatus.ACTIVE);
        card.setUser(user);
        Card savedCard = cardRepository.save(card);

        log.info("Создана карта ID={} для пользователя ID={}", savedCard.getId(), userId);
        return CardUtil.toDto(savedCard);
    }

    /**
     * Блокирует указанную банковскую карту.
     * <p>
     * Проверяет, что карта принадлежит текущему пользователю. Если нет — выбрасывает исключение.
     * Устанавливает статус карты в {@link CardStatus#BLOCKED}.
     *
     * @param cardId идентификатор карты, которую необходимо заблокировать
     * @throws NotFoundException если карта с указанным ID не найдена
     * @throws AccessDeniedException если карта не принадлежит текущему пользователю
     */
    @Override
    @Transactional
    public void blockCard(Long cardId) {
        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new NotFoundException("Карта с ID=" + cardId + " не найдена"));

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = auth.getName();
        if (!card.getUser().getUsername().equals(currentUsername)) {
            throw new AccessDeniedException("Карта не принадлежит текущему пользователю");
        }

        card.setStatus(CardStatus.BLOCKED);
        cardRepository.save(card);
        log.info("Карта ID={} заблокирована пользователем {}", cardId, currentUsername);
    }

    /**
     * Активирует указанную банковскую карту.
     * <p>
     * Устанавливает статус карты в {@link CardStatus#ACTIVE}. Может использоваться
     * администратором для активации новой карты.
     *
     * @param cardId идентификатор карты, которую необходимо активировать
     * @throws NotFoundException если карта с указанным ID не найдена
     */
    @Override
    @Transactional
    public void activateCard(Long cardId) {
        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new NotFoundException("Карта с ID=" + cardId + " не найдена"));

        card.setStatus(CardStatus.ACTIVE);
        cardRepository.save(card);
        log.info("Карта ID={} активирована", cardId);
    }

    /**
     * Удаляет банковскую карту из системы.
     * <p>
     * Операция безвозвратно удаляет карту по её идентификатору.
     * Может использоваться администратором для удаления ошибочно созданных или устаревших карт.
     *
     * @param cardId идентификатор карты, которую необходимо удалить
     * @throws NotFoundException если карта с указанным ID не найдена
     */
    @Override
    @Transactional
    public void deleteCard(Long cardId) {
        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new NotFoundException("Карта с ID=" + cardId + " не найдена"));

        cardRepository.delete(card);
        log.info("Карта ID={} удалена", cardId);
    }

    /**
     * Выполняет перевод средств между двумя банковскими картами.
     * <p>
     * Проверяет:
     * <ul>
     *   <li>Положительность суммы перевода</li>
     *   <li>Существование обеих карт</li>
     *   <li>Принадлежность карт одному пользователю</li>
     *   <li>Доступность средств на карте-отправителе</li>
     *   <li>Принадлежность карт текущему пользователю</li>
     * </ul>
     * При успешной проверке списывает сумму с одной карты и зачисляет на другую.
     *
     * @param fromCardId идентификатор карты-отправителя
     * @param toCardId   идентификатор карты-получателя
     * @param amount     сумма перевода; должна быть положительной
     * @throws InvalidAmountException если сумма перевода не положительная
     * @throws NotFoundException если одна из карт не найдена
     * @throws TransferNotAllowedException если карты принадлежат разным пользователям
     * @throws AccessDeniedException если карты не принадлежат текущему пользователю
     * @throws InsufficientFundsException если на карте-отправителе недостаточно средств
     */
    @Override
    @Transactional
    public void transfer(Long fromCardId, Long toCardId, Double amount) {
        if (amount <= 0) {
            throw new InvalidAmountException("Сумма перевода должна быть положительной");
        }

        Card fromCard = cardRepository.findById(fromCardId)
                .orElseThrow(() -> new NotFoundException("Исходная карта с ID=" + fromCardId + " не найдена"));
        Card toCard = cardRepository.findById(toCardId)
                .orElseThrow(() -> new NotFoundException("Целевая карта с ID=" + toCardId + " не найдена"));

        if (!fromCard.getUser().equals(toCard.getUser())) {
            throw new TransferNotAllowedException(
                    "Перевод между картами разных пользователей запрещён");
        }

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = auth.getName();
        if (!currentUsername.equals(fromCard.getUser().getUsername()) ||
                !currentUsername.equals(toCard.getUser().getUsername())) {
            throw new AccessDeniedException("Карты не принадлежат текущему пользователю");
        }

        if (fromCard.getBalance() < amount) {
            throw new InsufficientFundsException("На карте ID=" + fromCardId + " недостаточно средств");
        }

        fromCard.setBalance(fromCard.getBalance() - amount);
        toCard.setBalance(toCard.getBalance() + amount);

        cardRepository.save(fromCard);
        cardRepository.save(toCard);

        log.info("Выполнен перевод: {} руб. с карты {} на карту {}", amount, fromCardId, toCardId);
    }
}