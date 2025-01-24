package br.com.desafio.domain.usecase;

import br.com.desafio.domain.config.SQSClient;
import br.com.desafio.domain.model.PaymentItem;
import br.com.desafio.domain.model.Payment;
import br.com.desafio.exception.Exceptions.ClientNotFoundException;
import br.com.desafio.exception.Exceptions.PaymentItemNotFoundException;
import br.com.desafio.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ConfirmPaymentUseCaseImpl implements ConfirmPaymentUseCase {

    private final PaymentRepository paymentRepository;
    private final SQSClient sqsClient;

    @Override
    public Payment confirm(Payment payment) {
        Payment storedPayment = paymentRepository.findByClientId(payment.getClientId())
                .orElseThrow(() -> new ClientNotFoundException("Client ID " + payment.getClientId() + " not found."));

        for (PaymentItem item : payment.getPaymentItems()) {
            Optional<PaymentItem> matchedPaymentItem = findPaymentItemById(storedPayment, item.getPaymentId());

            if (matchedPaymentItem.isEmpty()) {
                throw new PaymentItemNotFoundException("Payment ID " + item.getPaymentId() + " not found.");
            }

            BigDecimal receivedValue = item.getPaymentValue();
            BigDecimal originalValue = matchedPaymentItem.get().getPaymentValue();

            setPaymentStatusForCurrentItem(item, receivedValue, originalValue);

            sqsClient.sendToQueueByPaymentStatus(item, item.getPaymentStatus());
        }

        return payment;
    }

    private void setPaymentStatusForCurrentItem(PaymentItem item, BigDecimal receivedValue, BigDecimal originalValue) {
        if (receivedValue.compareTo(originalValue) < 0) {
            item.setPaymentStatus("PARTIAL");
        } else if (receivedValue.compareTo(originalValue) == 0) {
            item.setPaymentStatus("TOTAL");
        } else {
            item.setPaymentStatus("EXCESS");
        }
    }

    private Optional<PaymentItem> findPaymentItemById(Payment payment, String paymentId) {
        return payment.getPaymentItems().stream()
                .filter(paymentItem -> paymentItem.getPaymentId().equals(paymentId))
                .findFirst();
    }
}
