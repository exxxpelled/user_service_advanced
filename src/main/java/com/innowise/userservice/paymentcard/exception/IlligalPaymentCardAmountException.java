package com.innowise.userservice.paymentcard.exception;

public class IlligalPaymentCardAmountException extends Exception {

  public IlligalPaymentCardAmountException() {
  }

  public IlligalPaymentCardAmountException(String message) {
    super(message);
  }

  public IlligalPaymentCardAmountException(String message, Throwable cause) {
    super(message, cause);
  }

  public IlligalPaymentCardAmountException(Throwable cause) {
    super(cause);
  }
}
