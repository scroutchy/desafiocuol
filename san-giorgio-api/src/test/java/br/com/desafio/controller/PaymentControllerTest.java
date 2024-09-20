package br.com.desafio.controller;

import br.com.desafio.domain.mapper.PaymentMapper;
import br.com.desafio.domain.model.PaymentModel;
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
public class PaymentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PaymentMapper paymentMapper;

    @MockBean
    private ConfirmPaymentUseCase confirmPaymentUseCase;

    @Autowired
    private ObjectMapper objectMapper;

    private Payment paymentRequest;
    private PaymentModel paymentModel;
    private PaymentModel updatedPaymentModel;
    private Payment responsePayment;

    @BeforeEach
    public void setUp() {
        paymentRequest = new Payment();
        paymentModel = new PaymentModel();
        updatedPaymentModel = new PaymentModel();
        responsePayment = new Payment();
    }

    @Test
    void testSetPayments_Success() throws Exception {
        when(paymentMapper.toPaymentModel(paymentRequest)).thenReturn(paymentModel);
        when(confirmPaymentUseCase.confirm(paymentModel)).thenReturn(updatedPaymentModel);
        when(paymentMapper.toPayment(updatedPaymentModel)).thenReturn(responsePayment);

        mockMvc.perform(post("/api/payments")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(paymentRequest)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(APPLICATION_JSON));

        verify(paymentMapper).toPaymentModel(paymentRequest);
        verify(confirmPaymentUseCase).confirm(paymentModel);
        verify(paymentMapper).toPayment(updatedPaymentModel);
    }

    @Test
    void testSetPayments_ClientNotFound() throws Exception {
        when(paymentMapper.toPaymentModel(paymentRequest)).thenReturn(paymentModel);
        when(confirmPaymentUseCase.confirm(paymentModel)).thenThrow(new ClientNotFoundException("Client not found"));

        mockMvc.perform(post("/api/payments")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(paymentRequest)))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(APPLICATION_JSON));
        // .andExpect(content().json("{\"message\":\"Client not found\"}"));

        verify(paymentMapper).toPaymentModel(paymentRequest);
        verify(confirmPaymentUseCase).confirm(paymentModel);
    }

    @Test
    void testSetPayments_PaymentItemNotFound() throws Exception {
        when(paymentMapper.toPaymentModel(paymentRequest)).thenReturn(paymentModel);
        when(confirmPaymentUseCase.confirm(paymentModel)).thenThrow(new PaymentItemNotFoundException("Payment item not found"));

        mockMvc.perform(post("/api/payments")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(paymentRequest)))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(APPLICATION_JSON));
        //.andExpect(content().json("{\"message\":\"Payment item not found\"}"));

        verify(paymentMapper).toPaymentModel(paymentRequest);
        verify(confirmPaymentUseCase).confirm(paymentModel);
    }

    @Test
    void testSetPayments_InvalidArgument() throws Exception {
        when(paymentMapper.toPaymentModel(paymentRequest)).thenReturn(paymentModel);
        when(confirmPaymentUseCase.confirm(paymentModel)).thenThrow(new IllegalArgumentException("Invalid payment"));

        mockMvc.perform(post("/api/payments")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(paymentRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(APPLICATION_JSON));
        //.andExpect(content().json("{\"message\":\"Invalid payment\"}"));

        verify(paymentMapper).toPaymentModel(paymentRequest);
        verify(confirmPaymentUseCase).confirm(paymentModel);
    }
}
