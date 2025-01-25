package br.com.desafio.domain.mapper;

import br.com.desafio.domain.model.api.PaymentApiDto;
import br.com.desafio.domain.model.api.PaymentItemApiDto;
import br.com.desafio.domain.model.entity.Payment;
import br.com.desafio.domain.model.entity.PaymentItem;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static java.util.Collections.singletonList;
import static org.junit.jupiter.api.Assertions.*;

class PaymentMapperTest {

    private PaymentMapper paymentMapper;

    @BeforeEach
    void setUp() {
        paymentMapper = new PaymentMapper();
    }

    @Test
    void testToPayment() {
        PaymentItemApiDto paymentItemApiDto1 = PaymentItemApiDto.builder()
                .paymentId("123")
                .paymentValue(BigDecimal.valueOf(100.00))
                .paymentStatus("PAID")
                .build();
        PaymentApiDto payment = PaymentApiDto.builder()
                .clientId("client123")
                .paymentItems(singletonList(paymentItemApiDto1))
                .build();

        Payment paymentModel = paymentMapper.toPayment(payment);

        assertNotNull(paymentModel);
        assertEquals("client123", paymentModel.getClientId());
        assertEquals(1, paymentModel.getPaymentItems().size());
        assertEquals("123", paymentModel.getPaymentItems().get(0).getPaymentId());
        assertEquals(BigDecimal.valueOf(100.00), paymentModel.getPaymentItems().get(0).getPaymentValue());
        assertNull(paymentModel.getPaymentItems().get(0).getPaymentStatus());
    }

    @Test
    void testToPaymentApiDto() {

        PaymentItem paymentItem1 = PaymentItem.builder()
                .paymentId("123")
                .paymentValue(BigDecimal.valueOf(100.00))
                .paymentStatus("PAID")
                .build();
        Payment paymentModel = Payment.builder()
                .clientId("client123")
                .paymentItems(singletonList(paymentItem1))
                .build();

        PaymentApiDto payment = paymentMapper.toPaymentApiDto(paymentModel);

        assertNotNull(payment);
        assertEquals("client123", payment.getClientId());
        assertEquals(1, payment.getPaymentItems().size());
        assertEquals("123", payment.getPaymentItems().get(0).getPaymentId());
        assertEquals(BigDecimal.valueOf(100.00), payment.getPaymentItems().get(0).getPaymentValue());
        assertEquals("PAID", payment.getPaymentItems().get(0).getPaymentStatus());
    }

    @Test
    void testToPaymentItemSqsDto() {
        var paymentItem = PaymentItem.builder()
                .paymentId("123")
                .paymentValue(BigDecimal.valueOf(100.00))
                .paymentStatus("PAID")
                .build();

        var paymentItemSqsDto = paymentMapper.toPaymentItemSqsDto(paymentItem);
        assertNotNull(paymentItemSqsDto);
        assertEquals("123", paymentItemSqsDto.getPaymentId());
        assertEquals(BigDecimal.valueOf(100.00), paymentItemSqsDto.getPaymentValue());
    }
}
