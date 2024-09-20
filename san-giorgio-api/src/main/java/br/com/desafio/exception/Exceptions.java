package br.com.desafio.exception;

public class Exceptions {

    public static class ClientNotFoundException extends RuntimeException {
        public ClientNotFoundException(String message) {
            super(message);
        }
    }

    public static class PaymentItemNotFoundException extends RuntimeException {
        public PaymentItemNotFoundException(String message) {
            super(message);
        }
    }
}
