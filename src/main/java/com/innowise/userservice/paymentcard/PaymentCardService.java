package com.innowise.userservice.paymentcard;

import com.innowise.userservice.paymentcard.dto.CreatePaymentCardRequest;
import com.innowise.userservice.paymentcard.dto.PaymentCardResponse;
import com.innowise.userservice.paymentcard.exception.IlligalPaymentCardAmountException;
import com.innowise.userservice.paymentcard.exception.PaymentCardNotFoundException;
import com.innowise.userservice.user.UserEntity;
import com.innowise.userservice.user.UserRepository;
import com.innowise.userservice.user.exception.UserNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PaymentCardService {

  private static final int MAX_AMOUNT_OF_CARDS = 5;
  private final PaymentCardRepository paymentCardRepository;
  private final PaymentCardMapper paymentCardMapper;
  private final UserRepository userRepository;

  @SneakyThrows
  @Transactional
  public PaymentCardResponse create(
          CreatePaymentCardRequest paymentCardDto
  ) {
    UserEntity foundUserEntity = userRepository.findById(paymentCardDto.userId())
            .orElseThrow(() -> new UserNotFoundException("User with id=" + paymentCardDto.userId() + " not found"));

    if (foundUserEntity.getCards().size() >= MAX_AMOUNT_OF_CARDS) {
      throw new IlligalPaymentCardAmountException("User can have no more than " + MAX_AMOUNT_OF_CARDS + " paymentCards");
    }

    PaymentCardEntity paymentCardEntityToSave = paymentCardMapper.toEntity(paymentCardDto, foundUserEntity);
    PaymentCardEntity savedPaymentCardEntity = paymentCardRepository.save(paymentCardEntityToSave);

    return paymentCardMapper.toDto(savedPaymentCardEntity);
  }

  @SneakyThrows
  @Transactional(readOnly = true)
  @Cacheable(
          value = "paymentCard",
          key = "#id"
  )
  public PaymentCardResponse getById(
          Long id
  ) {
    PaymentCardEntity foundPaymentCardEntity = paymentCardRepository.findById(id)
            .orElseThrow(() -> new PaymentCardNotFoundException("PaymentCardEntity with id=" + id + " not found"));
    return paymentCardMapper.toDto(foundPaymentCardEntity);
  }

  @Transactional(readOnly = true)
  public List<PaymentCardResponse> getAllBySpecification(
          String name,
          String surname,
          Pageable pageable
  ) {
    Specification<PaymentCardEntity> specification = Specification
            .where(PaymentCardSpecification.hasHolderName(name))
            .and(PaymentCardSpecification.hasHolderSurname(surname));
    return paymentCardRepository.findAll(specification, pageable).stream()
            .map(paymentCardMapper::toDto)
            .toList();
  }

  @Transactional(readOnly = true)
  public List<PaymentCardResponse> getCardsByUserId(
          Long userId,
          Pageable pageable
  ) {
    Page<PaymentCardEntity> foundPaymentCardEntities = paymentCardRepository.findAllByUserId(userId, pageable);
    return foundPaymentCardEntities.stream()
            .map(paymentCardMapper::toDto)
            .toList();
  }

  @SneakyThrows
  @Transactional
  @CacheEvict(
          value = "paymentCard",
          key = "#id"
  )
  public PaymentCardResponse setActiveStatus(
          Long id,
          Boolean active
  ) {
    PaymentCardEntity foundPaymentCardEntity = paymentCardRepository.findById(id)
            .orElseThrow(() -> new PaymentCardNotFoundException("PaymentCardEntity with id=" + id + " not found"));
    foundPaymentCardEntity.setActive(active);
    return paymentCardMapper.toDto(foundPaymentCardEntity);
  }

  @SneakyThrows
  @Transactional
  @CacheEvict(
          value = "paymentCard",
          key = "#id"
  )
  public void delete(
          Long id
  ) {
    PaymentCardEntity foundPaymentCardEntity = paymentCardRepository.findById(id)
            .orElseThrow(() -> new PaymentCardNotFoundException("PaymentCardEntity with id=" + id + " not found"));
    paymentCardRepository.delete(foundPaymentCardEntity);
  }
}