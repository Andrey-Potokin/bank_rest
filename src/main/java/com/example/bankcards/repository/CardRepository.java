package com.example.bankcards.repository;

import com.example.bankcards.entity.Card;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Репозиторий для управления сущностями {@link Card} (банковские карты).
 * <p>
 * Предоставляет методы для выполнения CRUD-операций и пагинации с использованием Spring Data JPA.
 * Наследует стандартные операции из {@link JpaRepository}, такие как сохранение, удаление,
 * поиск по идентификатору и другие.
 * <p>
 * Также содержит пользовательские методы для получения карт:
 * <ul>
 *   <li>По идентификатору пользователя с поддержкой пагинации</li>
 *   <li>Все карты системы с пагинацией</li>
 * </ul>
 */
@Repository
public interface CardRepository extends JpaRepository<Card, Long> {

    /**
     * Находит страницу с банковскими картами, принадлежащими пользователю с указанным ID.
     * <p>
     * Поддерживает пагинацию: позволяет получать карты порциями с указанием размера страницы,
     * номера страницы и параметров сортировки.
     *
     * @param userId   идентификатор пользователя, которому принадлежат карты; не может быть null
     * @param pageable параметры пагинации (размер, номер страницы, сортировка)
     * @return объект {@link Page}, содержащий список карт и метаданные пагинации
     */
    Page<Card> findByUserId(Long userId, Pageable pageable);

    /**
     * Возвращает страницу со всеми банковскими картами в системе.
     * <p>
     * Используется администратором для просмотра полного списка карт.
     * Поддерживает пагинацию и сортировку.
     *
     * @param pageable параметры пагинации (размер, номер страницы, сортировка)
     * @return объект {@link Page}, содержащий все карты и метаданные пагинации
     */
    Page<Card> findAll(Pageable pageable);
}