package br.com.desafio.controller;

import br.com.desafio.domain.mapper.PaymentMapper;
import br.com.desafio.domain.model.api.PaymentApiDto;
import br.com.desafio.domain.model.entity.Payment;
import br.com.desafio.domain.usecase.ConfirmPaymentUseCase;
import br.com.desafio.exception.Exceptions.ClientNotFoundException;
import br.com.desafio.exception.Exceptions.PaymentItemNotFoundException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PaymentController.class)
class PaymentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PaymentMapper paymentMapper;

    @MockBean
    private ConfirmPaymentUseCase confirmPaymentUseCase;

    @Autowired
    private ObjectMapper objectMapper;

    private PaymentApiDto paymentRequest;
    private Payment payment;
    private Payment updatedPayment;
    private PaymentApiDto responsePayment;

    @BeforeEach
    public void setUp() {
        paymentRequest = new PaymentApiDto();
        payment = new Payment();
        updatedPayment = new Payment();
        responsePayment = new PaymentApiDto();
    }

    @Test
    void testSetPayments_Success() throws Exception {
        when(paymentMapper.toPayment(paymentRequest)).thenReturn(payment);
        when(confirmPaymentUseCase.confirm(payment)).thenReturn(updatedPayment);
        when(paymentMapper.toPaymentApiDto(updatedPayment)).thenReturn(responsePayment);

        mockMvc.perform(post("/api/payments")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(paymentRequest)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(APPLICATION_JSON));

        verify(paymentMapper).toPayment(paymentRequest);
        verify(confirmPaymentUseCase).confirm(payment);
        verify(paymentMapper).toPaymentApiDto(updatedPayment);
    }

    @Test
    void testSetPayments_ClientNotFound() throws Exception {
        when(paymentMapper.toPayment(paymentRequest)).thenReturn(payment);
        when(confirmPaymentUseCase.confirm(payment)).thenThrow(new ClientNotFoundException("Client not found"));

        mockMvc.perform(post("/api/payments")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(paymentRequest)))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(APPLICATION_JSON));

        verify(paymentMapper).toPayment(paymentRequest);
        verify(confirmPaymentUseCase).confirm(payment);
    }

    @Test
    void testSetPayments_PaymentItemNotFound() throws Exception {
        when(paymentMapper.toPayment(paymentRequest)).thenReturn(payment);
        when(confirmPaymentUseCase.confirm(payment)).thenThrow(new PaymentItemNotFoundException("Payment item not found"));

        mockMvc.perform(post("/api/payments")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(paymentRequest)))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(APPLICATION_JSON));

        verify(paymentMapper).toPayment(paymentRequest);
        verify(confirmPaymentUseCase).confirm(payment);
    }

    @Test
    void testSetPayments_InvalidArgument() throws Exception {
        when(paymentMapper.toPayment(paymentRequest)).thenReturn(payment);
        when(confirmPaymentUseCase.confirm(payment)).thenThrow(new IllegalArgumentException("Invalid payment"));

        mockMvc.perform(post("/api/payments")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(paymentRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(APPLICATION_JSON));

        verify(paymentMapper).toPayment(paymentRequest);
        verify(confirmPaymentUseCase).confirm(payment);
    }
}
