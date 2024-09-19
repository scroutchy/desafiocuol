package br.com.desafio.exception;

public class PaymentItemNotFoundException extends RuntimeException {
    public PaymentItemNotFoundException(String message) {
        super(message);
    }
}
