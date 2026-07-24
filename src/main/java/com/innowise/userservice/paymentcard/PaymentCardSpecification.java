package com.innowise.userservice.paymentcard;

import org.springframework.data.jpa.domain.Specification;

public class PaymentCardSpecification {

  public static Specification<PaymentCardEntity> hasHolderName(
          String name
  ) {
    return (root, query, criteriaBuilder) -> {
      if (name == null || name.isEmpty()) {
        return criteriaBuilder.conjunction();
      }
      return criteriaBuilder.like(
              criteriaBuilder.lower(root.get("holder")), "%" + name.toLowerCase() + "%"
      );
    };
  }

  public static Specification<PaymentCardEntity> hasHolderSurname(
          String surname
  ) {
    return (root, query, criteriaBuilder) -> {
      if (surname == null || surname.isEmpty()) {
        return criteriaBuilder.conjunction();
      }
      return criteriaBuilder.like(
              criteriaBuilder.lower(root.get("holder")), "%" + surname.toLowerCase() + "%"
      );
    };
  }
}