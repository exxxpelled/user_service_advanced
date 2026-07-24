package com.innowise.userservice.user;

import com.innowise.userservice.user.dto.CreateUserRequest;
import com.innowise.userservice.user.dto.UpdateUserRequest;
import com.innowise.userservice.user.dto.UserResponse;
import com.innowise.userservice.user.exception.UserNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
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
    UserEntity userEntityToSave = userMapper.toEntity(userToCreate);
    UserEntity savedUserEntity = userRepository.save(userEntityToSave);
    return userMapper.toDto(savedUserEntity);
  }

  @Transactional(readOnly = true)
  @Cacheable(
          value = "user",
          key = "#id"
  )
  public UserResponse getById(Long id) {
    UserEntity foundUserEntity = findEntityById(id);
    return userMapper.toDto(foundUserEntity);
  }

  @Transactional(readOnly = true)
  public List<UserResponse> getAllByFilter(String name, String surname, Pageable pageable) {
    Specification<UserEntity> filter = Specification
            .where(UserSpecification.hasName(name))
            .and(UserSpecification.hasSurname(surname));

    return userRepository.findAll(filter, pageable).getContent().stream()
            .map(userMapper::toDto)
            .toList();
  }

  @Transactional
  @CachePut(
          value = "user",
          key = "#id"
  )
  public UserResponse update(Long id, UpdateUserRequest userToUpdate) {
    UserEntity foundUserEntity = findEntityById(id);
    userMapper.updateEntityFromDto(userToUpdate, foundUserEntity);
    return userMapper.toDto(foundUserEntity);
  }

  @Transactional
  @CacheEvict(
          value = "user",
          key = "#id"
  )
  public UserResponse setActiveStatus(Long id, Boolean active) {
    UserEntity foundUserEntity = findEntityById(id);
    foundUserEntity.setActive(active);
    return userMapper.toDto(foundUserEntity);
  }

  @Transactional
  @CacheEvict(
          value = "user",
          key = "#id"
  )
  public void delete(Long id) {
    UserEntity foundUserEntity = findEntityById(id);
    userRepository.delete(foundUserEntity);
  }

  private UserEntity findEntityById(Long id) {
    return userRepository.findById(id)
            .orElseThrow(() -> new UserNotFoundException("User with id=" + id + " not found"));
  }
}