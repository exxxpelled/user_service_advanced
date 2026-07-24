package com.innowise.userservice.user;

import com.innowise.userservice.user.dto.CreateUserRequest;
import com.innowise.userservice.user.dto.UpdateUserRequest;
import com.innowise.userservice.user.dto.UserResponse;
import com.innowise.userservice.user.exception.UserNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

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

  @SneakyThrows
  @Transactional(readOnly = true)
  @Cacheable(
          value = "user",
          key = "#id"
  )
  public UserResponse getById(Long id) {
    UserEntity foundUserEntity = userRepository.findById(id)
            .orElseThrow(() -> new UserNotFoundException("User with id=" + id + " not found"));
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

  @SneakyThrows
  @Transactional
  @CachePut(
          value = "user",
          key = "#id"
  )
  public UserResponse update(Long id, UpdateUserRequest userToUpdate) {
    UserEntity foundUserEntity = userRepository.findById(id)
            .orElseThrow(() -> new UserNotFoundException("User with id " + id + " not found"));

    userMapper.updateEntityFromDto(userToUpdate, foundUserEntity);
    return userMapper.toDto(foundUserEntity);
  }

  @SneakyThrows
  @Transactional
  @CacheEvict(
          value = "user",
          key = "#id"
  )
  public UserResponse setActiveStatus(Long id, Boolean active) {
    UserEntity foundUserEntity = userRepository.findById(id)
            .orElseThrow(() -> new UserNotFoundException("User with id=" + id + " not found"));
    foundUserEntity.setActive(active);
    return userMapper.toDto(foundUserEntity);
  }

  @SneakyThrows
  @Transactional
  @CacheEvict(
          value = "user",
          key = "#id"
  )
  public void delete(Long id) {
    UserEntity foundUserEntity = userRepository.findById(id)
            .orElseThrow(() -> new UserNotFoundException("User with id=" + id + " not found"));
    userRepository.delete(foundUserEntity);
  }
}