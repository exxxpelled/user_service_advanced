package com.innowise.userservice.paymentcard;

import com.innowise.userservice.paymentcard.dto.CreatePaymentCardRequest;
import com.innowise.userservice.paymentcard.dto.PaymentCardResponse;
import com.innowise.userservice.paymentcard.exception.IlligalPaymentCardAmountException;
import com.innowise.userservice.paymentcard.exception.PaymentCardNotFoundException;
import com.innowise.userservice.user.UserEntity;
import com.innowise.userservice.user.UserRepository;
import com.innowise.userservice.user.exception.UserNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PaymentCardServiceTest {

  @Mock
  private PaymentCardRepository paymentCardRepository;

  @Mock
  private PaymentCardMapper paymentCardMapper;

  @Mock
  private UserRepository userRepository;

  @InjectMocks
  private PaymentCardService paymentCardService;

  private UserEntity userEntity;
  private PaymentCardEntity paymentCardEntity;
  private PaymentCardResponse paymentCardResponse;
  private CreatePaymentCardRequest createPaymentCardRequest;
  private Long userId;
  private Long cardId;
  private LocalDate birthDate;
  private LocalDate expirationDate;

  @BeforeEach
  void setUp() {
    userId = 1L;
    cardId = 1L;
    birthDate = LocalDate.of(1990, 1, 1);
    expirationDate = LocalDate.now().plusYears(5);

    userEntity = new UserEntity();
    userEntity.setId(userId);
    userEntity.setName("Ivan");
    userEntity.setSurname("Ivanov");
    userEntity.setBirthDate(birthDate);
    userEntity.setEmail("Ivan.Ivanov@example.com");
    userEntity.setActive(true);
    userEntity.setCards(new ArrayList<>());

    paymentCardEntity = new PaymentCardEntity();
    paymentCardEntity.setId(cardId);
    paymentCardEntity.setNumber("1234567890123456");
    paymentCardEntity.setHolder("Ivan Ivanov");
    paymentCardEntity.setExpirationDate(expirationDate);
    paymentCardEntity.setActive(true);
    paymentCardEntity.setUser(userEntity);

    paymentCardResponse = new PaymentCardResponse(
            cardId,
            userId,
            "1234567890123456",
            "Ivan Ivanov",
            expirationDate,
            true
    );

    createPaymentCardRequest = new CreatePaymentCardRequest(
            userId,
            "1234567890123456"
    );
  }

  @Test
  void createShouldReturnPaymentCardDtoWhenUserExistsAndHasLessThanMaxCards() {
    when(userRepository.findById(userId)).thenReturn(Optional.of(userEntity));
    when(paymentCardMapper.toEntity(createPaymentCardRequest, userEntity)).thenReturn(paymentCardEntity);
    when(paymentCardRepository.save(paymentCardEntity)).thenReturn(paymentCardEntity);
    when(paymentCardMapper.toDto(paymentCardEntity)).thenReturn(paymentCardResponse);

    PaymentCardResponse result = paymentCardService.create(createPaymentCardRequest);

    assertThat(result).isNotNull();
    assertThat(result.id()).isEqualTo(cardId);
    assertThat(result.userId()).isEqualTo(userId);
    assertThat(result.number()).isEqualTo("1234567890123456");
    assertThat(result.holder()).isEqualTo("Ivan Ivanov");
    assertThat(result.active()).isTrue();

    verify(userRepository).findById(userId);
    verify(paymentCardMapper).toEntity(createPaymentCardRequest, userEntity);
    verify(paymentCardRepository).save(paymentCardEntity);
    verify(paymentCardMapper).toDto(paymentCardEntity);
  }

  @Test
  void createShouldThrowUserNotFoundExceptionWhenUserIvanovsNotExist() {
    when(userRepository.findById(userId)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> paymentCardService.create(createPaymentCardRequest))
            .isInstanceOf(UserNotFoundException.class)
            .hasMessage("User with id=" + userId + " not found");

    verify(userRepository).findById(userId);
    verify(paymentCardMapper, never()).toEntity(any(), any());
    verify(paymentCardRepository, never()).save(any());
    verify(paymentCardMapper, never()).toDto(any());
  }

  @Test
  void createShouldThrowIlligalPaymentCardAmountExceptionWhenUserHasMaxCards() {
    List<PaymentCardEntity> existingCards = new ArrayList<>();
    for (int i = 0; i < 5; i++) {
      PaymentCardEntity card = new PaymentCardEntity();
      card.setId((long) (i + 2));
      existingCards.add(card);
    }
    userEntity.setCards(existingCards);

    when(userRepository.findById(userId)).thenReturn(Optional.of(userEntity));

    assertThatThrownBy(() -> paymentCardService.create(createPaymentCardRequest))
            .isInstanceOf(IlligalPaymentCardAmountException.class)
            .hasMessage("User can have no more than 5 paymentCards");

    verify(userRepository).findById(userId);
    verify(paymentCardMapper, never()).toEntity(any(), any());
    verify(paymentCardRepository, never()).save(any());
    verify(paymentCardMapper, never()).toDto(any());
  }

  @Test
  void createShouldSucceedWhenUserHasExactlyMaxCardsMinusOne() {
    List<PaymentCardEntity> existingCards = new ArrayList<>();
    for (int i = 0; i < 4; i++) {
      PaymentCardEntity card = new PaymentCardEntity();
      card.setId((long) (i + 2));
      existingCards.add(card);
    }
    userEntity.setCards(existingCards);

    when(userRepository.findById(userId)).thenReturn(Optional.of(userEntity));
    when(paymentCardMapper.toEntity(createPaymentCardRequest, userEntity)).thenReturn(paymentCardEntity);
    when(paymentCardRepository.save(paymentCardEntity)).thenReturn(paymentCardEntity);
    when(paymentCardMapper.toDto(paymentCardEntity)).thenReturn(paymentCardResponse);

    PaymentCardResponse result = paymentCardService.create(createPaymentCardRequest);

    assertThat(result).isNotNull();
    assertThat(result.id()).isEqualTo(cardId);

    verify(userRepository).findById(userId);
    verify(paymentCardMapper).toEntity(createPaymentCardRequest, userEntity);
    verify(paymentCardRepository).save(paymentCardEntity);
    verify(paymentCardMapper).toDto(paymentCardEntity);
  }

  @Test
  void getByIdShouldReturnPaymentCardDtoWhenCardExists() {
    when(paymentCardRepository.findById(cardId)).thenReturn(Optional.of(paymentCardEntity));
    when(paymentCardMapper.toDto(paymentCardEntity)).thenReturn(paymentCardResponse);

    PaymentCardResponse result = paymentCardService.getById(cardId);

    assertThat(result).isNotNull();
    assertThat(result.id()).isEqualTo(cardId);
    assertThat(result.userId()).isEqualTo(userId);
    assertThat(result.number()).isEqualTo("1234567890123456");

    verify(paymentCardRepository).findById(cardId);
    verify(paymentCardMapper).toDto(paymentCardEntity);
  }

  @Test
  void getByIdShouldThrowPaymentCardNotFoundExceptionWhenCardIvanovsNotExist() {
    when(paymentCardRepository.findById(cardId)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> paymentCardService.getById(cardId))
            .isInstanceOf(PaymentCardNotFoundException.class)
            .hasMessage("PaymentCardEntity with id=" + cardId + " not found");

    verify(paymentCardRepository).findById(cardId);
    verify(paymentCardMapper, never()).toDto(any());
  }

  @Test
  void getAllByFilterShouldReturnListOfPaymentCardDtoWhenSpecificationApplied() {
    String name = "Ivan";
    String surname = "Ivanov";
    Pageable pageable = PageRequest.of(0, 10);

    List<PaymentCardEntity> cardEntities = List.of(paymentCardEntity);
    Page<PaymentCardEntity> page = new PageImpl<>(cardEntities, pageable, cardEntities.size());

    when(paymentCardRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(page);
    when(paymentCardMapper.toDto(paymentCardEntity)).thenReturn(paymentCardResponse);

    List<PaymentCardResponse> result = paymentCardService.getAllBySpecification(name, surname, pageable);

    assertThat(result).hasSize(1);
    assertThat(result.get(0).id()).isEqualTo(cardId);
    assertThat(result.get(0).userId()).isEqualTo(userId);

    verify(paymentCardRepository).findAll(any(Specification.class), eq(pageable));
    verify(paymentCardMapper).toDto(paymentCardEntity);
  }

  @Test
  void getAllBySpecificationShouldReturnEmptyListWhenNoCardsFound() {
    String name = "NonExistent";
    String surname = "User";
    Pageable pageable = PageRequest.of(0, 10);

    Page<PaymentCardEntity> page = new PageImpl<>(List.of(), pageable, 0);

    when(paymentCardRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(page);

    List<PaymentCardResponse> result = paymentCardService.getAllBySpecification(name, surname, pageable);

    assertThat(result).isEmpty();
    verify(paymentCardRepository).findAll(any(Specification.class), eq(pageable));
    verify(paymentCardMapper, never()).toDto(any());
  }

  @Test
  void getAllByFilterShouldHandleNullSpecificationParameters() {
    Pageable pageable = PageRequest.of(0, 10);
    List<PaymentCardEntity> cardEntities = List.of(paymentCardEntity);
    Page<PaymentCardEntity> page = new PageImpl<>(cardEntities, pageable, cardEntities.size());

    when(paymentCardRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(page);
    when(paymentCardMapper.toDto(paymentCardEntity)).thenReturn(paymentCardResponse);

    List<PaymentCardResponse> result = paymentCardService.getAllBySpecification(null, null, pageable);

    assertThat(result).hasSize(1);
    verify(paymentCardRepository).findAll(any(Specification.class), eq(pageable));
  }

  @Test
  void getAllByFilterShouldHandleEmptyStringSpecificationParameters() {
    Pageable pageable = PageRequest.of(0, 10);
    List<PaymentCardEntity> cardEntities = List.of(paymentCardEntity);
    Page<PaymentCardEntity> page = new PageImpl<>(cardEntities, pageable, cardEntities.size());

    when(paymentCardRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(page);
    when(paymentCardMapper.toDto(paymentCardEntity)).thenReturn(paymentCardResponse);

    List<PaymentCardResponse> result = paymentCardService.getAllBySpecification("", "", pageable);

    assertThat(result).hasSize(1);
    verify(paymentCardRepository).findAll(any(Specification.class), eq(pageable));
  }

  @Test
  void getCardsByUserIdShouldReturnListOfPaymentCardDtoWhenUserHasCards() {
    Pageable pageable = PageRequest.of(0, 10);
    List<PaymentCardEntity> cardEntities = List.of(paymentCardEntity);
    Page<PaymentCardEntity> page = new PageImpl<>(cardEntities, pageable, cardEntities.size());

    when(paymentCardRepository.findAllByUserId(userId, pageable)).thenReturn(page);
    when(paymentCardMapper.toDto(paymentCardEntity)).thenReturn(paymentCardResponse);

    List<PaymentCardResponse> result = paymentCardService.getCardsByUserId(userId, pageable);

    assertThat(result).hasSize(1);
    assertThat(result.get(0).id()).isEqualTo(cardId);
    assertThat(result.get(0).userId()).isEqualTo(userId);

    verify(paymentCardRepository).findAllByUserId(userId, pageable);
    verify(paymentCardMapper).toDto(paymentCardEntity);
  }

  @Test
  void getCardsByUserIdShouldReturnEmptyListWhenUserHasNoCards() {
    Pageable pageable = PageRequest.of(0, 10);
    Page<PaymentCardEntity> page = new PageImpl<>(List.of(), pageable, 0);

    when(paymentCardRepository.findAllByUserId(userId, pageable)).thenReturn(page);

    List<PaymentCardResponse> result = paymentCardService.getCardsByUserId(userId, pageable);

    assertThat(result).isEmpty();
    verify(paymentCardRepository).findAllByUserId(userId, pageable);
    verify(paymentCardMapper, never()).toDto(any());
  }

  @Test
  void getCardsByUserIdShouldHandlePagination() {
    Pageable pageable = PageRequest.of(1, 5);
    List<PaymentCardEntity> cardEntities = List.of(paymentCardEntity);
    Page<PaymentCardEntity> page = new PageImpl<>(cardEntities, pageable, cardEntities.size());

    when(paymentCardRepository.findAllByUserId(userId, pageable)).thenReturn(page);
    when(paymentCardMapper.toDto(paymentCardEntity)).thenReturn(paymentCardResponse);

    List<PaymentCardResponse> result = paymentCardService.getCardsByUserId(userId, pageable);

    assertThat(result).hasSize(1);
    verify(paymentCardRepository).findAllByUserId(userId, pageable);
  }

  @Test
  void setActiveStatusShouldReturnUpdatedPaymentCardDtoWhenCardExists() {
    Boolean newActiveStatus = false;

    when(paymentCardRepository.findById(cardId)).thenReturn(Optional.of(paymentCardEntity));
    when(paymentCardMapper.toDto(paymentCardEntity)).thenReturn(
            new PaymentCardResponse(
                    cardId,
                    userId,
                    "1234567890123456",
                    "Ivan Ivanov",
                    expirationDate,
                    false
            )
    );

    PaymentCardResponse result = paymentCardService.setActiveStatus(cardId, newActiveStatus);

    assertThat(result).isNotNull();
    assertThat(result.active()).isFalse();
    assertThat(result.id()).isEqualTo(cardId);

    verify(paymentCardRepository).findById(cardId);
    verify(paymentCardMapper).toDto(paymentCardEntity);
  }

  @Test
  void setActiveStatusShouldThrowPaymentCardNotFoundExceptionWhenCardIvanovsNotExist() {
    when(paymentCardRepository.findById(cardId)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> paymentCardService.setActiveStatus(cardId, false))
            .isInstanceOf(PaymentCardNotFoundException.class)
            .hasMessage("PaymentCardEntity with id=" + cardId + " not found");

    verify(paymentCardRepository).findById(cardId);
    verify(paymentCardMapper, never()).toDto(any());
  }

  @Test
  void setActiveStatusShouldActivateCard() {
    Boolean activeStatus = true;

    when(paymentCardRepository.findById(cardId)).thenReturn(Optional.of(paymentCardEntity));
    when(paymentCardMapper.toDto(paymentCardEntity)).thenReturn(
            new PaymentCardResponse(
                    cardId,
                    userId,
                    "1234567890123456",
                    "Ivan Ivanov",
                    expirationDate,
                    true
            )
    );

    PaymentCardResponse result = paymentCardService.setActiveStatus(cardId, activeStatus);

    assertThat(result).isNotNull();
    assertThat(result.active()).isTrue();
    assertThat(result.id()).isEqualTo(cardId);

    verify(paymentCardRepository).findById(cardId);
    verify(paymentCardMapper).toDto(paymentCardEntity);
  }

  @Test
  void deleteShouldDeleteCardWhenCardExists() {
    when(paymentCardRepository.findById(cardId)).thenReturn(Optional.of(paymentCardEntity));
    doNothing().when(paymentCardRepository).delete(any(PaymentCardEntity.class));

    paymentCardService.delete(cardId);

    verify(paymentCardRepository).findById(cardId);
    verify(paymentCardRepository).delete(any(PaymentCardEntity.class));
  }

  @Test
  void createShouldSetCardWithCorrectUserReference() {
    when(userRepository.findById(userId)).thenReturn(Optional.of(userEntity));
    when(paymentCardMapper.toEntity(createPaymentCardRequest, userEntity)).thenReturn(paymentCardEntity);
    when(paymentCardRepository.save(paymentCardEntity)).thenReturn(paymentCardEntity);
    when(paymentCardMapper.toDto(paymentCardEntity)).thenReturn(paymentCardResponse);

    PaymentCardResponse result = paymentCardService.create(createPaymentCardRequest);

    assertThat(result).isNotNull();
    assertThat(result.userId()).isEqualTo(userId);

    verify(paymentCardMapper).toEntity(createPaymentCardRequest, userEntity);
    verify(paymentCardRepository).save(paymentCardEntity);
  }

  @Test
  void createShouldSetDefaultValuesForHolderAndExpirationDate() {
    PaymentCardEntity newCardEntity = new PaymentCardEntity();
    newCardEntity.setId(2L);
    newCardEntity.setNumber("9876543210987654");
    newCardEntity.setHolder("Ivan Ivanov");
    newCardEntity.setExpirationDate(LocalDate.now().plusYears(5));
    newCardEntity.setActive(true);
    newCardEntity.setUser(userEntity);

    PaymentCardResponse newCardDto = new PaymentCardResponse(
            2L,
            userId,
            "9876543210987654",
            "Ivan Ivanov",
            LocalDate.now().plusYears(5),
            true
    );

    CreatePaymentCardRequest newCreateDto = new CreatePaymentCardRequest(
            userId,
            "9876543210987654"
    );

    when(userRepository.findById(userId)).thenReturn(Optional.of(userEntity));
    when(paymentCardMapper.toEntity(newCreateDto, userEntity)).thenReturn(newCardEntity);
    when(paymentCardRepository.save(newCardEntity)).thenReturn(newCardEntity);
    when(paymentCardMapper.toDto(newCardEntity)).thenReturn(newCardDto);

    PaymentCardResponse result = paymentCardService.create(newCreateDto);

    assertThat(result).isNotNull();
    assertThat(result.holder()).isEqualTo("Ivan Ivanov");
    assertThat(result.active()).isTrue();

    verify(paymentCardMapper).toEntity(newCreateDto, userEntity);
    verify(paymentCardRepository).save(newCardEntity);
    verify(paymentCardMapper).toDto(newCardEntity);
  }

  @Test
  void getByIdShouldThrowPaymentCardNotFoundExceptionWithCorrectMessage() {
    Long nonExistentId = 999L;
    when(paymentCardRepository.findById(nonExistentId)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> paymentCardService.getById(nonExistentId))
            .isInstanceOf(PaymentCardNotFoundException.class)
            .hasMessage("PaymentCardEntity with id=" + nonExistentId + " not found");
  }

  @Test
  void deleteShouldThrowPaymentCardNotFoundExceptionWithCorrectMessage() {
    Long nonExistentId = 999L;
    when(paymentCardRepository.findById(nonExistentId)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> paymentCardService.delete(nonExistentId))
            .isInstanceOf(PaymentCardNotFoundException.class)
            .hasMessage("PaymentCardEntity with id=" + nonExistentId + " not found");
  }

  @Test
  void setActiveStatusShouldThrowPaymentCardNotFoundExceptionWithCorrectMessage() {
    Long nonExistentId = 999L;
    when(paymentCardRepository.findById(nonExistentId)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> paymentCardService.setActiveStatus(nonExistentId, true))
            .isInstanceOf(PaymentCardNotFoundException.class)
            .hasMessage("PaymentCardEntity with id=" + nonExistentId + " not found");
  }
}