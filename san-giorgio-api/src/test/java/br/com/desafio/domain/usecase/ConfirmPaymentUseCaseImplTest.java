package br.com.desafio.domain.usecase;

import br.com.desafio.controller.SQSClient;
import br.com.desafio.domain.model.PaymentItemModel;
import br.com.desafio.domain.model.PaymentModel;
import br.com.desafio.exception.Exceptions.ClientNotFoundException;
import br.com.desafio.exception.Exceptions.PaymentItemNotFoundException;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.math.BigDecimal;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ConfirmPaymentUseCaseImplTest {

    private final SQSClient sqsClient = Mockito.mock(SQSClient.class);
    private final ConfirmPaymentUseCaseImpl confirmPaymentUseCase = new ConfirmPaymentUseCaseImpl(sqsClient);

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
                .clientId("C999") // clientId inválido
                .paymentItems(Collections.singletonList(paymentItemModel))
                .build();

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

        var exception = assertThrows(PaymentItemNotFoundException.class, () -> confirmPaymentUseCase.confirm(paymentModel));

        assertEquals("Payment ID invalid_payment_id not found.", exception.getMessage());
    }
}
