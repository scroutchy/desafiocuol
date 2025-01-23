package br.com.desafio.domain.usecase;

import br.com.desafio.domain.config.SQSClient;
import br.com.desafio.domain.model.PaymentItemModel;
import br.com.desafio.domain.model.PaymentModel;
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

    private final SQSClient sqsClient = mock(SQSClient.class);
    private final PaymentRepository paymentRepository = mock(PaymentRepository.class);
    private final ConfirmPaymentUseCaseImpl confirmPaymentUseCase = new ConfirmPaymentUseCaseImpl(paymentRepository, sqsClient);

    @Test
    void testConfirmPaymentWithPartialStatus() {
        PaymentItemModel paymentItemModel = PaymentItemModel.builder()
                .paymentId("P001")
                .paymentValue(new BigDecimal("50.00"))
                .build();
        PaymentModel paymentModel = PaymentModel.builder()
                .clientId("C001")
                .paymentItems(Collections.singletonList(paymentItemModel))
                .build();

        PaymentModel storedPayment = PaymentModel.builder()
                .clientId("C001")
                .paymentItems(Collections.singletonList(PaymentItemModel.builder()
                        .paymentId("P001")
                        .paymentValue(new BigDecimal("100.00"))
                        .build()))
                .build();
        Mockito.when(paymentRepository.findByClientId("C001")).thenReturn(Optional.of(storedPayment));

        PaymentModel result = confirmPaymentUseCase.confirm(paymentModel);

        assertEquals("PARTIAL", result.getPaymentItems().get(0).getPaymentStatus());
    }

    @Test
    void testInvalidClientId() {
        PaymentItemModel paymentItemModel = PaymentItemModel.builder()
                .paymentId("P001")
                .paymentValue(new BigDecimal("50.00"))
                .build();
        PaymentModel paymentModel = PaymentModel.builder()
                .clientId("C999")
                .paymentItems(Collections.singletonList(paymentItemModel))
                .build();

        Mockito.when(paymentRepository.findByClientId("C999")).thenReturn(Optional.empty());

        var exception = assertThrows(ClientNotFoundException.class, () -> confirmPaymentUseCase.confirm(paymentModel));

        assertEquals("Client ID C999 not found.", exception.getMessage());
    }

    @Test
    void testInvalidPaymentId() {
        PaymentItemModel paymentItemModel = PaymentItemModel.builder()
                .paymentId("invalid_payment_id")
                .paymentValue(new BigDecimal("50.00"))
                .build();
        PaymentModel paymentModel = PaymentModel.builder()
                .clientId("C001")
                .paymentItems(Collections.singletonList(paymentItemModel))
                .build();


        PaymentModel storedPayment = PaymentModel.builder()
                .clientId("C001")
                .paymentItems(Collections.singletonList(PaymentItemModel.builder()
                        .paymentId("P001")
                        .paymentValue(new BigDecimal("100.00"))
                        .build()))
                .build();
        Mockito.when(paymentRepository.findByClientId("C001")).thenReturn(Optional.of(storedPayment));

        var exception = assertThrows(PaymentItemNotFoundException.class, () -> confirmPaymentUseCase.confirm(paymentModel));

        assertEquals("Payment ID invalid_payment_id not found.", exception.getMessage());
    }
}
