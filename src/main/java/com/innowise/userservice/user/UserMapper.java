package com.innowise.userservice.user;

import com.innowise.userservice.paymentcard.PaymentCardMapper;
import com.innowise.userservice.user.dto.CreateUserRequest;
import com.innowise.userservice.user.dto.UpdateUserRequest;
import com.innowise.userservice.user.dto.UserResponse;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(
        componentModel = "spring",
        uses = PaymentCardMapper.class,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface UserMapper {

  @Mapping(target = "id",  ignore = true)
  @Mapping(target = "active", constant = "true")
  @Mapping(target = "cards", ignore = true)
  UserEntity toEntity(CreateUserRequest createUserRequest);

  UserResponse toDto(UserEntity userEntity);

  @Mapping(target = "id", ignore = true)
  @Mapping(target = "active", ignore = true)
  @Mapping(target = "cards", ignore = true)
  void updateEntityFromDto(UpdateUserRequest updateUserRequest, @MappingTarget UserEntity entity);

  @AfterMapping
  default void linkPaymentCards(@MappingTarget UserEntity userEntity) {
    if (userEntity.getCards() != null) {
      userEntity.getCards().forEach(card -> card.setUser(userEntity));
    }
  }
}