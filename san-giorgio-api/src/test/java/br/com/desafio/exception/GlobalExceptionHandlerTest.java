package br.com.desafio.exception;

import br.com.desafio.controller.ErrorResponse;
import br.com.desafio.exception.Exceptions.ClientNotFoundException;
import br.com.desafio.exception.Exceptions.PaymentItemNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler globalExceptionHandler;

    @BeforeEach
    public void setUp() {
        globalExceptionHandler = new GlobalExceptionHandler();
    }

    @Test
    void testHandleClientNotFoundException() {
        ClientNotFoundException exception = new ClientNotFoundException("Client not found");

        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleClientNotFoundException(exception);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("Client not found", response.getBody().getErrorMessage());
    }

    @Test
    void testHandlePaymentItemNotFoundException() {
        PaymentItemNotFoundException exception = new PaymentItemNotFoundException("Payment item not found");

        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handlePaymentItemNotFoundException(exception);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("Payment item not found", response.getBody().getErrorMessage());
    }

    @Test
    void testHandleIllegalArgumentException() {
        IllegalArgumentException exception = new IllegalArgumentException("Invalid argument");

        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleIllegalArgumentException(exception);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Invalid argument", response.getBody().getErrorMessage());
    }
}
