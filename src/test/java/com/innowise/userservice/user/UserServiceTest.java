package com.innowise.userservice.user;

import com.innowise.userservice.paymentcard.PaymentCardEntity;
import com.innowise.userservice.user.dto.CreateUserRequest;
import com.innowise.userservice.user.dto.UpdateUserRequest;
import com.innowise.userservice.user.dto.UserResponse;
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
class UserServiceTest {

  @Mock
  private UserRepository userRepository;

  @Mock
  private UserMapper userMapper;

  @InjectMocks
  private UserService userService;

  private UserEntity userEntity;
  private UserResponse userResponse;
  private CreateUserRequest createUserRequest;
  private UpdateUserRequest updateUserRequest;
  private Long userId;
  private LocalDate birthDate;

  @BeforeEach
  void setUp() {
    userId = 1L;
    birthDate = LocalDate.of(1990, 1, 1);

    userEntity = new UserEntity();
    userEntity.setId(userId);
    userEntity.setName("Ivan");
    userEntity.setSurname("Ivanov");
    userEntity.setBirthDate(birthDate);
    userEntity.setEmail("Ivan.Ivanov@example.com");
    userEntity.setActive(true);

    userResponse = new UserResponse(
            userId,
            "Ivan",
            "Ivanov",
            birthDate,
            "Ivan.Ivanov@example.com",
            true,
            List.of()
    );

    createUserRequest = new CreateUserRequest(
            "Ivan",
            "Ivanov",
            birthDate,
            "Ivan.Ivanov@example.com"
    );

    updateUserRequest = new UpdateUserRequest(
            "Oleg",
            "Smith",
            birthDate,
            "Oleg.smith@example.com"
    );
  }

  @Test
  void createShouldReturnUserDtoWhenUserIsValid() {
    when(userMapper.toEntity(createUserRequest)).thenReturn(userEntity);
    when(userRepository.save(userEntity)).thenReturn(userEntity);
    when(userMapper.toDto(userEntity)).thenReturn(userResponse);

    UserResponse result = userService.create(createUserRequest);

    assertThat(result).isNotNull();
    assertThat(result.id()).isEqualTo(userId);
    assertThat(result.name()).isEqualTo("Ivan");
    assertThat(result.surname()).isEqualTo("Ivanov");
    assertThat(result.email()).isEqualTo("Ivan.Ivanov@example.com");
    assertThat(result.active()).isTrue();

    verify(userMapper).toEntity(createUserRequest);
    verify(userRepository).save(userEntity);
    verify(userMapper).toDto(userEntity);
  }

  @Test
  void getByIdShouldReturnUserDtoWhenUserExists() {
    when(userRepository.findById(userId)).thenReturn(Optional.of(userEntity));
    when(userMapper.toDto(userEntity)).thenReturn(userResponse);

    UserResponse result = userService.getById(userId);

    assertThat(result).isNotNull();
    assertThat(result.id()).isEqualTo(userId);
    assertThat(result.name()).isEqualTo("Ivan");
    assertThat(result.surname()).isEqualTo("Ivanov");

    verify(userRepository).findById(userId);
    verify(userMapper).toDto(userEntity);
  }

  @Test
  void getByIdShouldThrowUserNotFoundExceptionWhenUserIvanovsNotExist() {
    when(userRepository.findById(userId)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> userService.getById(userId))
            .isInstanceOf(UserNotFoundException.class)
            .hasMessage("User with id=" + userId + " not found");

    verify(userRepository).findById(userId);
    verify(userMapper, never()).toDto(any());
  }

  @Test
  void getAllByFilterShouldReturnListOfUserDtoWhenFilterApplied() {
    String name = "Ivan";
    String surname = "Ivanov";
    Pageable pageable = PageRequest.of(0, 10);

    List<UserEntity> userEntities = List.of(userEntity);
    Page<UserEntity> page = new PageImpl<>(userEntities, pageable, userEntities.size());

    when(userRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(page);
    when(userMapper.toDto(userEntity)).thenReturn(userResponse);

    List<UserResponse> result = userService.getAllByFilter(name, surname, pageable);

    assertThat(result).hasSize(1);
    assertThat(result.get(0).id()).isEqualTo(userId);
    assertThat(result.get(0).name()).isEqualTo("Ivan");

    verify(userRepository).findAll(any(Specification.class), eq(pageable));
    verify(userMapper).toDto(userEntity);
  }

  @Test
  void getAllByFilterShouldReturnEmptyListWhenNoUsersFound() {
    String name = "NonExistent";
    String surname = "User";
    Pageable pageable = PageRequest.of(0, 10);

    Page<UserEntity> page = new PageImpl<>(List.of(), pageable, 0);

    when(userRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(page);

    List<UserResponse> result = userService.getAllByFilter(name, surname, pageable);

    assertThat(result).isEmpty();
    verify(userRepository).findAll(any(Specification.class), eq(pageable));
    verify(userMapper, never()).toDto(any());
  }

  @Test
  void getAllByFilterShouldHandleNullFilterParameters() {
    Pageable pageable = PageRequest.of(0, 10);
    List<UserEntity> userEntities = List.of(userEntity);
    Page<UserEntity> page = new PageImpl<>(userEntities, pageable, userEntities.size());

    when(userRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(page);
    when(userMapper.toDto(userEntity)).thenReturn(userResponse);

    List<UserResponse> result = userService.getAllByFilter(null, null, pageable);

    assertThat(result).hasSize(1);
    verify(userRepository).findAll(any(Specification.class), eq(pageable));
  }

  @Test
  void getAllByFilterShouldHandleEmptyStringFilterParameters() {
    Pageable pageable = PageRequest.of(0, 10);
    List<UserEntity> userEntities = List.of(userEntity);
    Page<UserEntity> page = new PageImpl<>(userEntities, pageable, userEntities.size());

    when(userRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(page);
    when(userMapper.toDto(userEntity)).thenReturn(userResponse);

    List<UserResponse> result = userService.getAllByFilter("", "", pageable);

    assertThat(result).hasSize(1);
    verify(userRepository).findAll(any(Specification.class), eq(pageable));
  }

  @Test
  void updateShouldReturnUpdatedUserDtoWhenUserExists() {
    UserEntity updatedUserEntity = new UserEntity();
    updatedUserEntity.setId(userId);
    updatedUserEntity.setName("Oleg");
    updatedUserEntity.setSurname("Smith");
    updatedUserEntity.setBirthDate(birthDate);
    updatedUserEntity.setEmail("Oleg.smith@example.com");
    updatedUserEntity.setActive(true);

    UserResponse updatedUserResponse = new UserResponse(
            userId,
            "Oleg",
            "Smith",
            birthDate,
            "Oleg.smith@example.com",
            true,
            List.of()
    );

    when(userRepository.findById(userId)).thenReturn(Optional.of(userEntity));
    doNothing().when(userMapper).updateEntityFromDto(updateUserRequest, userEntity);
    when(userMapper.toDto(userEntity)).thenReturn(updatedUserResponse);

    UserResponse result = userService.update(userId, updateUserRequest);

    assertThat(result).isNotNull();
    assertThat(result.name()).isEqualTo("Oleg");
    assertThat(result.surname()).isEqualTo("Smith");
    assertThat(result.email()).isEqualTo("Oleg.smith@example.com");

    verify(userRepository).findById(userId);
    verify(userMapper).updateEntityFromDto(updateUserRequest, userEntity);
    verify(userMapper).toDto(userEntity);
  }

  @Test
  void updateShouldThrowUserNotFoundExceptionWhenUserIvanovsNotExist() {
    when(userRepository.findById(userId)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> userService.update(userId, updateUserRequest))
            .isInstanceOf(UserNotFoundException.class)
            .hasMessage("User with id " + userId + " not found");

    verify(userRepository).findById(userId);
    verify(userMapper, never()).updateEntityFromDto(any(), any());
    verify(userMapper, never()).toDto(any());
  }

  @Test
  void updateShouldUpdateOnlyProvidedFields() {
    UpdateUserRequest partialUpdateDto = new UpdateUserRequest(
            "Oleg",
            null,
            null,
            null
    );

    UserResponse updatedUserResponse = new UserResponse(
            userId,
            "Oleg",
            "Ivanov",
            birthDate,
            "Ivan.Ivanov@example.com",
            true,
            List.of()
    );

    when(userRepository.findById(userId)).thenReturn(Optional.of(userEntity));
    doNothing().when(userMapper).updateEntityFromDto(partialUpdateDto, userEntity);
    when(userMapper.toDto(userEntity)).thenReturn(updatedUserResponse);

    UserResponse result = userService.update(userId, partialUpdateDto);

    assertThat(result).isNotNull();
    assertThat(result.name()).isEqualTo("Oleg");
    assertThat(result.surname()).isEqualTo("Ivanov");
    assertThat(result.email()).isEqualTo("Ivan.Ivanov@example.com");

    verify(userMapper).updateEntityFromDto(partialUpdateDto, userEntity);
  }

  @Test
  void setActiveStatusShouldReturnUpdatedUserDtoWhenUserExists() {
    Boolean newActiveStatus = false;

    when(userRepository.findById(userId)).thenReturn(Optional.of(userEntity));
    when(userMapper.toDto(userEntity)).thenReturn(
            new UserResponse(
                    userId,
                    "Ivan",
                    "Ivanov",
                    birthDate,
                    "Ivan.Ivanov@example.com",
                    false,
                    List.of()
            )
    );

    UserResponse result = userService.setActiveStatus(userId, newActiveStatus);

    assertThat(result).isNotNull();
    assertThat(result.active()).isFalse();

    verify(userRepository).findById(userId);
    verify(userMapper).toDto(userEntity);
  }

  @Test
  void setActiveStatusShouldThrowUserNotFoundExceptionWhenUserIvanovsNotExist() {
    when(userRepository.findById(userId)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> userService.setActiveStatus(userId, false))
            .isInstanceOf(UserNotFoundException.class)
            .hasMessage("User with id=" + userId + " not found");

    verify(userRepository).findById(userId);
    verify(userMapper, never()).toDto(any());
  }

  @Test
  void setActiveStatusShouldActivateUser() {
    Boolean activeStatus = true;

    when(userRepository.findById(userId)).thenReturn(Optional.of(userEntity));
    when(userMapper.toDto(userEntity)).thenReturn(
            new UserResponse(
                    userId,
                    "Ivan",
                    "Ivanov",
                    birthDate,
                    "Ivan.Ivanov@example.com",
                    true,
                    List.of()
            )
    );

    UserResponse result = userService.setActiveStatus(userId, activeStatus);

    assertThat(result).isNotNull();
    assertThat(result.active()).isTrue();

    verify(userRepository).findById(userId);
    verify(userMapper).toDto(userEntity);
  }

  @Test
  void deleteShouldDeleteUserWhenUserExists() {
    when(userRepository.findById(userId)).thenReturn(Optional.of(userEntity));
    doNothing().when(userRepository).delete(any(UserEntity.class));

    userService.delete(userId);

    verify(userRepository).findById(userId);
    verify(userRepository).delete(any(UserEntity.class));
  }

  @Test
  void createShouldSetActiveToTrueByDefault() {
    UserEntity newUserEntity = new UserEntity();
    newUserEntity.setId(2L);
    newUserEntity.setName("Petr");
    newUserEntity.setSurname("Petrov");
    newUserEntity.setBirthDate(birthDate);
    newUserEntity.setEmail("Petr@example.com");
    newUserEntity.setActive(true);

    UserResponse newUserResponse = new UserResponse(
            2L,
            "Petr",
            "Petrov",
            birthDate,
            "Petr@example.com",
            true,
            List.of()
    );

    CreateUserRequest newCreateUserRequest = new CreateUserRequest(
            "Petr",
            "Petrov",
            birthDate,
            "Petr@example.com"
    );

    when(userMapper.toEntity(newCreateUserRequest)).thenReturn(newUserEntity);
    when(userRepository.save(newUserEntity)).thenReturn(newUserEntity);
    when(userMapper.toDto(newUserEntity)).thenReturn(newUserResponse);

    UserResponse result = userService.create(newCreateUserRequest);

    assertThat(result).isNotNull();
    assertThat(result.active()).isTrue();
    assertThat(result.name()).isEqualTo("Petr");
    assertThat(result.surname()).isEqualTo("Petrov");

    verify(userMapper).toEntity(newCreateUserRequest);
    verify(userRepository).save(newUserEntity);
    verify(userMapper).toDto(newUserEntity);
  }

  @Test
  void createShouldHandleNullFieldsGracefully() {
    CreateUserRequest createUserRequestWithNulls = new CreateUserRequest(
            null,
            null,
            null,
            null
    );

    UserEntity userEntityWithNulls = new UserEntity();
    userEntityWithNulls.setId(2L);
    userEntityWithNulls.setActive(true);

    UserResponse userResponseWithNulls = new UserResponse(
            2L,
            null,
            null,
            null,
            null,
            true,
            List.of()
    );

    when(userMapper.toEntity(createUserRequestWithNulls)).thenReturn(userEntityWithNulls);
    when(userRepository.save(userEntityWithNulls)).thenReturn(userEntityWithNulls);
    when(userMapper.toDto(userEntityWithNulls)).thenReturn(userResponseWithNulls);

    UserResponse result = userService.create(createUserRequestWithNulls);

    assertThat(result).isNotNull();
    assertThat(result.id()).isEqualTo(2L);

    verify(userMapper).toEntity(createUserRequestWithNulls);
    verify(userRepository).save(userEntityWithNulls);
    verify(userMapper).toDto(userEntityWithNulls);
  }

  @Test
  void getByIdShouldHandleUserWithPaymentCards() {
    PaymentCardEntity cardEntity = new PaymentCardEntity();
    cardEntity.setId(1L);
    cardEntity.setNumber("1234567890123456");
    cardEntity.setActive(true);

    List<PaymentCardEntity> cards = List.of(cardEntity);
    userEntity.setCards(cards);

    when(userRepository.findById(userId)).thenReturn(Optional.of(userEntity));
    when(userMapper.toDto(userEntity)).thenReturn(userResponse);

    UserResponse result = userService.getById(userId);

    assertThat(result).isNotNull();
    assertThat(result.id()).isEqualTo(userId);
    assertThat(result.cards()).isEmpty();

    verify(userRepository).findById(userId);
    verify(userMapper).toDto(userEntity);
  }
}