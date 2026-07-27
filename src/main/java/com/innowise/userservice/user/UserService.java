package com.innowise.userservice.user;

import com.innowise.userservice.user.dto.CreateUserRequest;
import com.innowise.userservice.user.dto.UpdateUserRequest;
import com.innowise.userservice.user.dto.UserResponse;
import com.innowise.userservice.user.exception.UserNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

  private final UserRepository userRepository;
  private final UserMapper userMapper;

  @Transactional
  public UserResponse create(CreateUserRequest userToCreate) {
    log.debug("Attempting to create user with name='{}', surname='{}'",
            userToCreate.name(), userToCreate.surname());

    UserEntity userEntityToSave = userMapper.toEntity(userToCreate);
    UserEntity savedUserEntity = userRepository.save(userEntityToSave);

    log.info("Successfully created user with id={}", savedUserEntity.getId());
    return userMapper.toDto(savedUserEntity);
  }

  @Transactional(readOnly = true)
  @Cacheable(
          value = "user",
          key = "#id"
  )
  public UserResponse getById(Long id) {
    log.debug("Fetching user by id={}", id);
    UserEntity foundUserEntity = findEntityById(id);
    return userMapper.toDto(foundUserEntity);
  }

  @Transactional(readOnly = true)
  public List<UserResponse> getAllByFilter(String name, String surname, Pageable pageable) {
    log.debug("Fetching users by filter: name='{}', surname='{}', pageable={}", name, surname, pageable);

    Specification<UserEntity> filter = Specification
            .where(UserSpecification.hasName(name))
            .and(UserSpecification.hasSurname(surname));

    List<UserResponse> foundUsers = userRepository.findAll(filter, pageable).getContent().stream()
            .map(userMapper::toDto)
            .toList();

    log.debug("Found {} users for filter: name='{}', surname='{}'", foundUsers.size(), name, surname);
    return foundUsers;
  }

  @Transactional
  @CachePut(
          value = "user",
          key = "#id"
  )
  public UserResponse update(Long id, UpdateUserRequest userToUpdate) {
    log.debug("Attempting to update user with id={}", id);

    UserEntity foundUserEntity = findEntityById(id);
    userMapper.updateEntityFromDto(userToUpdate, foundUserEntity);

    log.info("Successfully updated user with id={}", id);
    return userMapper.toDto(foundUserEntity);
  }

  @Transactional
  @CacheEvict(
          value = "user",
          key = "#id"
  )
  public UserResponse setActiveStatus(Long id, Boolean active) {
    log.debug("Attempting to update active status to active={} for user id={}", active, id);

    UserEntity foundUserEntity = findEntityById(id);
    foundUserEntity.setActive(active);

    log.info("Successfully updated active status to active={} for user id={}", active, id);
    return userMapper.toDto(foundUserEntity);
  }

  @Transactional
  @CacheEvict(
          value = "user",
          key = "#id"
  )
  public void delete(Long id) {
    log.debug("Attempting to delete user with id={}", id);

    UserEntity foundUserEntity = findEntityById(id);
    userRepository.delete(foundUserEntity);

    log.info("Successfully deleted user with id={}", id);
  }

  private UserEntity findEntityById(Long id) {
    return userRepository.findById(id)
            .orElseThrow(() -> {
              log.warn("User not found with id={}", id);
              return new UserNotFoundException("User with id=" + id + " not found");
            });
  }
}