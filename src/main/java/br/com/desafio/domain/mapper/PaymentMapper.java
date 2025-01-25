package br.com.desafio.domain.mapper;

import br.com.desafio.domain.model.api.PaymentApiDto;
import br.com.desafio.domain.model.api.PaymentItemApiDto;
import br.com.desafio.domain.model.entity.Payment;
import br.com.desafio.domain.model.entity.PaymentItem;
import br.com.desafio.domain.model.sqs.PaymentItemSqsDto;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class PaymentMapper {

    public Payment toPayment(PaymentApiDto payment) {
        List<PaymentItem> paymentItems = payment.getPaymentItems().stream()
                .map(this::toPaymentItem)
                .toList();

        return Payment.builder()
                .clientId(payment.getClientId())
                .paymentItems(paymentItems)
                .build();
    }

    private PaymentItem toPaymentItem(PaymentItemApiDto paymentItemApiDto) {
        return PaymentItem.builder()
                .paymentId(paymentItemApiDto.getPaymentId())
                .paymentValue(paymentItemApiDto.getPaymentValue())
                .build();
    }

    public PaymentApiDto toPaymentApiDto(Payment payment) {
        List<PaymentItemApiDto> paymentItemApiDtos = payment.getPaymentItems().stream()
                .map(this::toPaymentItemApiDto)
                .toList();

        return PaymentApiDto.builder()
                .clientId(payment.getClientId())
                .paymentItems(paymentItemApiDtos)
                .build();
    }

    private PaymentItemApiDto toPaymentItemApiDto(PaymentItem paymentItem) {
        return PaymentItemApiDto.builder()
                .paymentId(paymentItem.getPaymentId())
                .paymentValue(paymentItem.getPaymentValue())
                .paymentStatus(paymentItem.getPaymentStatus())
                .build();
    }

    public PaymentItemSqsDto toPaymentItemSqsDto(PaymentItem paymentItem) {
        return PaymentItemSqsDto.builder()
                .paymentId(paymentItem.getPaymentId())
                .paymentValue(paymentItem.getPaymentValue())
                .build();
    }
}
