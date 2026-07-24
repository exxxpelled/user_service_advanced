package com.innowise.userservice.paymentcard.exception;

public class PaymentCardNotFoundException extends Exception {
  public PaymentCardNotFoundException() {
  }

  public PaymentCardNotFoundException(String message) {
    super(message);
  }

  public PaymentCardNotFoundException(String message, Throwable cause) {
    super(message, cause);
  }

  public PaymentCardNotFoundException(Throwable cause) {
    super(cause);
  }
}
