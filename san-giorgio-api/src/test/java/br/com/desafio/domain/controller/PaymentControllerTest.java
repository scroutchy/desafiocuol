package br.com.desafio.domain.controller;

import br.com.desafio.controller.Payment;
import br.com.desafio.controller.PaymentController;
import br.com.desafio.domain.mapper.PaymentMapper;
import br.com.desafio.domain.model.PaymentModel;
import br.com.desafio.domain.usecase.ConfirmPaymentUseCase;
import br.com.desafio.exception.ClientNotFoundException;
import br.com.desafio.exception.PaymentItemNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpStatus.*;

class PaymentControllerTest {

    @InjectMocks
    private PaymentController paymentController;

    @Mock
    private PaymentMapper paymentMapper;

    @Mock
    private ConfirmPaymentUseCase confirmPaymentUseCase;

    private Payment requestPayment;
    private PaymentModel paymentModel;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        // Inicializando objetos de exemplo
        requestPayment = new Payment();
        paymentModel = new PaymentModel();
    }

    @Test
    void testSetPayment_Success() {
        // Dado
        when(paymentMapper.toPaymentModel(requestPayment)).thenReturn(paymentModel);
        when(confirmPaymentUseCase.confirm(paymentModel)).thenReturn(paymentModel);
        when(paymentMapper.toPayment(paymentModel)).thenReturn(requestPayment);

        // Quando
        ResponseEntity<?> response = paymentController.setPayment(requestPayment);

        // Então
        assertEquals(OK, response.getStatusCode());
        assertEquals(requestPayment, response.getBody());

        verify(paymentMapper).toPaymentModel(requestPayment);
        verify(confirmPaymentUseCase).confirm(paymentModel);
        verify(paymentMapper).toPayment(paymentModel);
    }

    @Test
    void testSetPayment_ClientNotFound() {
        // Dado
        when(paymentMapper.toPaymentModel(requestPayment)).thenReturn(paymentModel);
        when(confirmPaymentUseCase.confirm(paymentModel)).thenThrow(new ClientNotFoundException("Client not found"));

        // Quando
        ResponseEntity<?> response = paymentController.setPayment(requestPayment);

        // Então
        assertEquals(NOT_FOUND, response.getStatusCode());
    }

    @Test
    void testSetPayment_PaymentItemNotFound() {
        // Dado
        when(paymentMapper.toPaymentModel(requestPayment)).thenReturn(paymentModel);
        when(confirmPaymentUseCase.confirm(paymentModel)).thenThrow(new PaymentItemNotFoundException("Payment item not found"));

        // Quando
        ResponseEntity<?> response = paymentController.setPayment(requestPayment);

        // Então
        assertEquals(NOT_FOUND, response.getStatusCode());
    }

    @Test
    void testSetPayment_InvalidInput() {
        // Dado
        when(paymentMapper.toPaymentModel(requestPayment)).thenThrow(new IllegalArgumentException("Invalid payment"));

        // Quando
        ResponseEntity<?> response = paymentController.setPayment(requestPayment);

        // Então
        assertEquals(BAD_REQUEST, response.getStatusCode());
    }
}
