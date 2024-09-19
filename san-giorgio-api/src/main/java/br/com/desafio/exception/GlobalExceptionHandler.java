package br.com.desafio.exception;

import br.com.desafio.controller.ErrorResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ClientNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleClientNotFoundException(ClientNotFoundException e) {
        return ResponseEntity.status(NOT_FOUND).body(new ErrorResponse(e.getMessage()));
    }

    @ExceptionHandler(PaymentItemNotFoundException.class)
    public ResponseEntity<ErrorResponse> handlePaymentItemNotFoundException(PaymentItemNotFoundException e) {
        return ResponseEntity.status(NOT_FOUND).body(new ErrorResponse(e.getMessage()));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgumentException(IllegalArgumentException e) {
        return ResponseEntity.status(BAD_REQUEST).body(new ErrorResponse(e.getMessage()));
    }
}
