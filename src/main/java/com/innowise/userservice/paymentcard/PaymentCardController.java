package com.innowise.userservice.paymentcard;

import com.innowise.userservice.paymentcard.dto.CreatePaymentCardRequest;
import com.innowise.userservice.paymentcard.dto.PaymentCardResponse;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/cards")
@AllArgsConstructor
public class PaymentCardController {

  private static final int DEFAULT_PAGE_SIZE = 10;
  private static final int DEFAULT_PAGE_NUMBER = 0;
  private final PaymentCardService paymentCardService;

  @PostMapping
  public ResponseEntity<PaymentCardResponse> createCard(
          @RequestBody @Valid CreatePaymentCardRequest paymentCardDto
  ) {
    return ResponseEntity.status(HttpStatus.CREATED)
            .body(paymentCardService.create(paymentCardDto));
  }

  @GetMapping("/{id}")
  public ResponseEntity<PaymentCardResponse> getCardById(
          @PathVariable(name = "id") Long id
  ) {
    return ResponseEntity.status(HttpStatus.OK)
            .body(paymentCardService.getById(id));
  }

  @GetMapping
  public ResponseEntity<List<PaymentCardResponse>> getAllCardsBySpecification(
          @RequestParam(name = "name", required = false) String name,
          @RequestParam(name = "surname", required = false) String surname,
          @RequestParam(name = "pageSize", required = false) Integer pageSize,
          @RequestParam(name = "pageNumber", required = false) Integer pageNumber
  ) {
    pageSize = pageSize != null
            ? pageSize
            : DEFAULT_PAGE_SIZE;
    pageNumber = pageNumber != null
            ? pageNumber
            : DEFAULT_PAGE_NUMBER;

    Pageable pageable = Pageable.ofSize(pageSize)
            .withPage(pageNumber);

    return ResponseEntity.status(HttpStatus.OK)
            .body(paymentCardService.getAllBySpecification(name, surname, pageable));
  }

  @GetMapping("/user/{userId}")
  public ResponseEntity<List<PaymentCardResponse>> getCardsByUserId(
          @PathVariable(name = "userId") Long userId,
          @RequestParam(name = "pageSize", required = false) Integer pageSize,
          @RequestParam(name = "pageNumber", required = false) Integer pageNumber
  ) {
    pageSize = pageSize != null
            ? pageSize
            : DEFAULT_PAGE_SIZE;
    pageNumber = pageNumber != null
            ? pageNumber
            : DEFAULT_PAGE_NUMBER;

    Pageable pageable = Pageable.ofSize(pageSize)
            .withPage(pageNumber);

    return ResponseEntity.status(HttpStatus.OK)
            .body(paymentCardService.getCardsByUserId(userId, pageable));
  }

  @PatchMapping("/{id}/active")
  public ResponseEntity<PaymentCardResponse> updateCardActiveStatus(
          @PathVariable(name = "id") Long id,
          @RequestParam(name = "active") Boolean active
  ) {
    return ResponseEntity.status(HttpStatus.OK)
            .body(paymentCardService.setActiveStatus(id,active));
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deleteCardById(
          @PathVariable(name = "id") Long id
  ) {
    paymentCardService.delete(id);
    return ResponseEntity.noContent().build();
  }
}