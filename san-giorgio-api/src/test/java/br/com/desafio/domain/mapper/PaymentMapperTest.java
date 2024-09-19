package br.com.desafio.domain.mapper;

import br.com.desafio.controller.Payment;
import br.com.desafio.controller.PaymentItem;
import br.com.desafio.domain.model.PaymentItemModel;
import br.com.desafio.domain.model.PaymentModel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static java.util.Collections.singletonList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class PaymentMapperTest {

    private PaymentMapper paymentMapper;

    @BeforeEach
    void setUp() {
        paymentMapper = new PaymentMapper();
    }

    @Test
    void testToPaymentModel() {
        // Arrange
        PaymentItem paymentItem1 = PaymentItem.builder()
                .paymentId("123")
                .paymentValue(BigDecimal.valueOf(100.00))
                .paymentStatus("PAID")
                .build();
        Payment payment = Payment.builder()
                .clientId("client123")
                .paymentItems(singletonList(paymentItem1))
                .build();

        // Act
        PaymentModel paymentModel = paymentMapper.toPaymentModel(payment);

        // Assert
        assertNotNull(paymentModel);
        assertEquals("client123", paymentModel.getClientId());
        assertEquals(1, paymentModel.getPaymentItems().size());
        assertEquals("123", paymentModel.getPaymentItems().get(0).getPaymentId());
        assertEquals(BigDecimal.valueOf(100.00), paymentModel.getPaymentItems().get(0).getPaymentValue());
        assertEquals("PAID", paymentModel.getPaymentItems().get(0).getPaymentStatus());
    }

    @Test
    void testToPayment() {
        // Arrange
        PaymentItemModel paymentItemModel1 = PaymentItemModel.builder()
                .paymentId("123")
                .paymentValue(BigDecimal.valueOf(100.00))
                .paymentStatus("PAID")
                .build();
        PaymentModel paymentModel = PaymentModel.builder()
                .clientId("client123")
                .paymentItems(singletonList(paymentItemModel1))
                .build();

        // Act
        Payment payment = paymentMapper.toPayment(paymentModel);

        // Assert
        assertNotNull(payment);
        assertEquals("client123", payment.getClientId());
        assertEquals(1, payment.getPaymentItems().size());
        assertEquals("123", payment.getPaymentItems().get(0).getPaymentId());
        assertEquals(BigDecimal.valueOf(100.00), payment.getPaymentItems().get(0).getPaymentValue());
        assertEquals("PAID", payment.getPaymentItems().get(0).getPaymentStatus());
    }
}
