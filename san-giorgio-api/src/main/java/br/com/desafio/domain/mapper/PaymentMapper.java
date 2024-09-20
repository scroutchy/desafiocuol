package br.com.desafio.domain.mapper;

import br.com.desafio.controller.Payment;
import br.com.desafio.controller.PaymentItem;
import br.com.desafio.domain.model.PaymentItemModel;
import br.com.desafio.domain.model.PaymentModel;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class PaymentMapper {

    public PaymentModel toPaymentModel(Payment payment) {
        List<PaymentItemModel> paymentItems = payment.getPaymentItems().stream()
                .map(this::toPaymentItemModel)
                .collect(Collectors.toList());

        return PaymentModel.builder()
                .clientId(payment.getClientId())
                .paymentItems(paymentItems)
                .build();
    }

    private PaymentItemModel toPaymentItemModel(PaymentItem paymentItem) {
        return PaymentItemModel.builder()
                .paymentId(paymentItem.getPaymentId())
                .paymentValue(paymentItem.getPaymentValue())
                .paymentStatus(paymentItem.getPaymentStatus())
                .build();
    }

    public Payment toPayment(PaymentModel paymentModel) {
        List<PaymentItem> paymentItems = paymentModel.getPaymentItems().stream()
                .map(this::toPaymentItem)
                .collect(Collectors.toList());

        return Payment.builder()
                .clientId(paymentModel.getClientId())
                .paymentItems(paymentItems)
                .build();
    }

    private PaymentItem toPaymentItem(PaymentItemModel paymentItemModel) {
        return PaymentItem.builder()
                .paymentId(paymentItemModel.getPaymentId())
                .paymentValue(paymentItemModel.getPaymentValue())
                .paymentStatus(paymentItemModel.getPaymentStatus())
                .build();
    }
}
