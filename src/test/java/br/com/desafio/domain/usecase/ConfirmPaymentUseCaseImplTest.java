package br.com.desafio.domain.usecase;

import br.com.desafio.domain.config.PaymentSqsClient;
import br.com.desafio.domain.model.Payment;
import br.com.desafio.domain.model.PaymentItem;
import br.com.desafio.exception.Exceptions.ClientNotFoundException;
import br.com.desafio.exception.Exceptions.PaymentItemNotFoundException;
import br.com.desafio.repository.PaymentRepository;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

class ConfirmPaymentUseCaseImplTest {

    private final PaymentSqsClient paymentSqsClient = mock(PaymentSqsClient.class);
    private final PaymentRepository paymentRepository = mock(PaymentRepository.class);
    private final ConfirmPaymentUseCaseImpl confirmPaymentUseCase = new ConfirmPaymentUseCaseImpl(paymentRepository, paymentSqsClient);

    @Test
    void testConfirmPaymentWithPartialStatus() {
        PaymentItem paymentItem = PaymentItem.builder()
                .paymentId("P001")
                .paymentValue(new BigDecimal("50.00"))
                .build();
        Payment payment = Payment.builder()
                .clientId("C001")
                .paymentItems(Collections.singletonList(paymentItem))
                .build();

        Payment storedPayment = Payment.builder()
                .clientId("C001")
                .paymentItems(Collections.singletonList(PaymentItem.builder()
                        .paymentId("P001")
                        .paymentValue(new BigDecimal("100.00"))
                        .build()))
                .build();
        Mockito.when(paymentRepository.findByClientId("C001")).thenReturn(Optional.of(storedPayment));

        Payment result = confirmPaymentUseCase.confirm(payment);

        assertEquals("PARTIAL", result.getPaymentItems().get(0).getPaymentStatus());
    }

    @Test
    void testInvalidClientId() {
        PaymentItem paymentItem = PaymentItem.builder()
                .paymentId("P001")
                .paymentValue(new BigDecimal("50.00"))
                .build();
        Payment payment = Payment.builder()
                .clientId("C999")
                .paymentItems(Collections.singletonList(paymentItem))
                .build();

        Mockito.when(paymentRepository.findByClientId("C999")).thenReturn(Optional.empty());

        var exception = assertThrows(ClientNotFoundException.class, () -> confirmPaymentUseCase.confirm(payment));

        assertEquals("Client ID C999 not found.", exception.getMessage());
    }

    @Test
    void testInvalidPaymentId() {
        PaymentItem paymentItem = PaymentItem.builder()
                .paymentId("invalid_payment_id")
                .paymentValue(new BigDecimal("50.00"))
                .build();
        Payment payment = Payment.builder()
                .clientId("C001")
                .paymentItems(Collections.singletonList(paymentItem))
                .build();


        Payment storedPayment = Payment.builder()
                .clientId("C001")
                .paymentItems(Collections.singletonList(PaymentItem.builder()
                        .paymentId("P001")
                        .paymentValue(new BigDecimal("100.00"))
                        .build()))
                .build();
        Mockito.when(paymentRepository.findByClientId("C001")).thenReturn(Optional.of(storedPayment));

        var exception = assertThrows(PaymentItemNotFoundException.class, () -> confirmPaymentUseCase.confirm(payment));

        assertEquals("Payment ID invalid_payment_id not found.", exception.getMessage());
    }
}
