package com.innowise.userservice.paymentcard;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface PaymentCardRepository extends JpaRepository<PaymentCardEntity, Long>, JpaSpecificationExecutor<PaymentCardEntity> {

  @Query(value = """
          SELECT COUNT(*) FROM payment_cards
          WHERE user_id = :userId
          """, nativeQuery = true)
  int countByUserId(
          @Param("userId") Long userId
  );

  boolean existsByNumber(String number);

  Page<PaymentCardEntity> findAllByUserId(Long userId, Pageable pageable);
}