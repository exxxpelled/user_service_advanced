package com.innowise.userservice.paymentcard;

import com.innowise.userservice.paymentcard.dto.CreatePaymentCardRequest;
import com.innowise.userservice.paymentcard.dto.PaymentCardResponse;
import com.innowise.userservice.paymentcard.exception.IllegalPaymentCardAmountException;
import com.innowise.userservice.paymentcard.exception.PaymentCardNotFoundException;
import com.innowise.userservice.user.UserEntity;
import com.innowise.userservice.user.UserRepository;
import com.innowise.userservice.user.UserService;
import com.innowise.userservice.user.dto.UserResponse;
import com.innowise.userservice.user.exception.UserNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentCardService {

  private static final int MAX_AMOUNT_OF_CARDS = 5;
  private final PaymentCardRepository paymentCardRepository;
  private final PaymentCardMapper paymentCardMapper;
  private final UserService userService;

  @Transactional
  public PaymentCardResponse create(
          CreatePaymentCardRequest paymentCardDto
  ) {
    log.debug("Attempting to create payment card for userId={}", paymentCardDto.userId());

    UserResponse foundUserDto = userService.getById(paymentCardDto.userId());

    if (foundUserDto.cards().size() >= MAX_AMOUNT_OF_CARDS) {
      log.warn("Failed to create payment card. User can have no more than {} paymentCards", MAX_AMOUNT_OF_CARDS);
      throw new IllegalPaymentCardAmountException("User can have no more than " + MAX_AMOUNT_OF_CARDS + " paymentCards");
    }

    PaymentCardEntity paymentCardEntityToSave = paymentCardMapper.toEntity(paymentCardDto, foundUserDto);
    PaymentCardEntity savedPaymentCardEntity = paymentCardRepository.save(paymentCardEntityToSave);

    log.info("Successfully created payment card with id={} for userId={}", savedPaymentCardEntity.getId(), savedPaymentCardEntity.getUser().getId());
    return paymentCardMapper.toDto(savedPaymentCardEntity);
  }

  @Transactional(readOnly = true)
  @Cacheable(
          value = "paymentCard",
          key = "#id"
  )
  public PaymentCardResponse getById(
          Long id
  ) {
    log.debug("Fetching payment card by id={}", id);
    PaymentCardEntity foundPaymentCardEntity = findPaymentCardEntityById(id);
    return paymentCardMapper.toDto(foundPaymentCardEntity);
  }

  @Transactional(readOnly = true)
  public List<PaymentCardResponse> getAllBySpecification(
          String name,
          String surname,
          Pageable pageable
  ) {
    log.debug("Fetching payment cards by specification: name='{}', surname='{}', pageable={}", name, surname, pageable);

    Specification<PaymentCardEntity> specification = Specification
            .where(PaymentCardSpecification.hasHolderName(name))
            .and(PaymentCardSpecification.hasHolderSurname(surname));

    List<PaymentCardResponse> foundedCards = paymentCardRepository.findAll(specification, pageable).stream()
            .map(paymentCardMapper::toDto)
            .toList();

    log.debug("Found {} payment cards for specification: name='{}', surname='{}'", foundedCards.size(), name, surname);
    return foundedCards;
  }

  @Transactional(readOnly = true)
  public List<PaymentCardResponse> getCardsByUserId(
          Long userId,
          Pageable pageable
  ) {
    log.debug("Fetching payment cards for userId={}, pageable={}", userId, pageable);

    Page<PaymentCardEntity> foundPaymentCardEntities = paymentCardRepository.findAllByUserId(userId, pageable);

    log.debug("Found {} payment cards for userId={}", foundPaymentCardEntities.getNumberOfElements(), userId);
    return foundPaymentCardEntities.stream()
            .map(paymentCardMapper::toDto)
            .toList();
  }

  @Transactional
  @CacheEvict(
          value = "paymentCard",
          key = "#id"
  )
  public PaymentCardResponse setActiveStatus(
          Long id,
          Boolean active
  ) {
    PaymentCardEntity foundPaymentCardEntity = findPaymentCardEntityById(id);
    foundPaymentCardEntity.setActive(active);

    log.info("Successfully updated active status to active={} for payment card id={}", active, id);

    return paymentCardMapper.toDto(foundPaymentCardEntity);
  }

  @Transactional
  @CacheEvict(
          value = "paymentCard",
          key = "#id"
  )
  public void delete(
          Long id
  ) {
    log.debug("Attempting to delete payment card with id={}", id);

    PaymentCardEntity foundPaymentCardEntity = findPaymentCardEntityById(id);
    paymentCardRepository.delete(foundPaymentCardEntity);

    log.info("Successfully deleted payment card with id={}", id);
  }

  private PaymentCardEntity findPaymentCardEntityById(Long id) {
    return paymentCardRepository.findById(id)
            .orElseThrow(() -> {
              log.warn("Payment card not found with id={}", id);
              return new PaymentCardNotFoundException("PaymentCardEntity with id=" + id + " not found");
            });
  }
}