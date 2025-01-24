package br.com.desafio.domain.mapper;

import br.com.desafio.controller.PaymentApiDto;
import br.com.desafio.controller.PaymentItemApiDto;
import br.com.desafio.domain.model.PaymentItem;
import br.com.desafio.domain.model.Payment;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class PaymentMapper {

    public Payment toPayment(PaymentApiDto payment) {
        List<PaymentItem> paymentItems = payment.getPaymentItems().stream()
                .map(this::toPaymentItem)
                .collect(Collectors.toList());

        return Payment.builder()
                .clientId(payment.getClientId())
                .paymentItems(paymentItems)
                .build();
    }

    private PaymentItem toPaymentItem(PaymentItemApiDto paymentItemApiDto) {
        return PaymentItem.builder()
                .paymentId(paymentItemApiDto.getPaymentId())
                .paymentValue(paymentItemApiDto.getPaymentValue())
                .paymentStatus(paymentItemApiDto.getPaymentStatus())
                .build();
    }

    public PaymentApiDto toPaymentApiDto(Payment payment) {
        List<PaymentItemApiDto> paymentItemApiDtos = payment.getPaymentItems().stream()
                .map(this::toPaymentItemApiDto)
                .collect(Collectors.toList());

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
}
