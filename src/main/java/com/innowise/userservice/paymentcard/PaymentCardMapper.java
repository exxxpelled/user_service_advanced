package com.innowise.userservice.paymentcard;

import com.innowise.userservice.paymentcard.dto.CreatePaymentCardRequest;
import com.innowise.userservice.paymentcard.dto.PaymentCardResponse;
import com.innowise.userservice.user.UserEntity;
import com.innowise.userservice.user.dto.UserResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.time.LocalDate;

@Mapper(
        componentModel = "spring",
        imports = LocalDate.class,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface PaymentCardMapper {

  @Mapping(target = "id", ignore = true)
  @Mapping(target = "number", source = "createPaymentCardRequest.number")
  @Mapping(target = "user.id", source = "userDto.id")
  @Mapping(target = "holder", expression = "java(userDto.name() + \" \" + userDto.surname())")
  @Mapping(target = "expirationDate", expression = "java(LocalDate.now().plusYears(5))")
  @Mapping(target = "active", constant = "true")
  PaymentCardEntity toEntity(CreatePaymentCardRequest createPaymentCardRequest, UserResponse userDto);

  @Mapping(target = "userId", source = "user.id")
  PaymentCardResponse toDto(PaymentCardEntity paymentCardEntity);
}