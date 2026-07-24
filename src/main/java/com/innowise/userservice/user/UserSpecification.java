package com.innowise.userservice.user;

import org.springframework.data.jpa.domain.Specification;

public class UserSpecification {
  public static Specification<UserEntity> hasName(
          String name
  ) {
    return (root, query, criteriaBuilder) -> {
      if (name == null || name.isEmpty()) {
        return criteriaBuilder.conjunction();
      }
      return criteriaBuilder.equal(
              criteriaBuilder.lower(root.get("name")), name.toLowerCase()
      );
    };
  }

  public static Specification<UserEntity> hasSurname(
          String surname
  ) {
    return (root, query, criteriaBuilder) -> {
      if (surname == null || surname.isEmpty()) {
        return criteriaBuilder.conjunction();
      }
      return criteriaBuilder.equal(
              criteriaBuilder.lower(root.get("surname")), surname.toLowerCase()
      );
    };
  }
}
