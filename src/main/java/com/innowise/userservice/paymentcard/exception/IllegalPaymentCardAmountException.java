package com.innowise.userservice.paymentcard.exception;

public class IllegalPaymentCardAmountException extends RuntimeException {

  public IllegalPaymentCardAmountException() {
  }

  public IllegalPaymentCardAmountException(String message) {
    super(message);
  }

  public IllegalPaymentCardAmountException(String message, Throwable cause) {
    super(message, cause);
  }

  public IllegalPaymentCardAmountException(Throwable cause) {
    super(cause);
  }
}
