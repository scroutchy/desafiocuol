package br.com.desafio.domain.usecase;

import br.com.desafio.domain.config.SQSClient;
import br.com.desafio.domain.model.PaymentItemModel;
import br.com.desafio.domain.model.PaymentModel;
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
    public PaymentModel confirm(PaymentModel paymentModel) {
        PaymentModel storedPayment = paymentRepository.findByClientId(paymentModel.getClientId())
                .orElseThrow(() -> new ClientNotFoundException("Client ID " + paymentModel.getClientId() + " not found."));

        for (PaymentItemModel item : paymentModel.getPaymentItems()) {
            Optional<PaymentItemModel> matchedPaymentItem = findPaymentItemById(storedPayment, item.getPaymentId());

            if (matchedPaymentItem.isEmpty()) {
                throw new PaymentItemNotFoundException("Payment ID " + item.getPaymentId() + " not found.");
            }

            BigDecimal receivedValue = item.getPaymentValue();
            BigDecimal originalValue = matchedPaymentItem.get().getPaymentValue();

            setPaymentStatusForCurrentItem(item, receivedValue, originalValue);

            sqsClient.sendToQueueByPaymentStatus(paymentModel, item.getPaymentStatus());
        }

        return paymentModel;
    }

    private void setPaymentStatusForCurrentItem(PaymentItemModel item, BigDecimal receivedValue, BigDecimal originalValue) {
        if (receivedValue.compareTo(originalValue) < 0) {
            item.setPaymentStatus("PARTIAL");
        } else if (receivedValue.compareTo(originalValue) == 0) {
            item.setPaymentStatus("TOTAL");
        } else {
            item.setPaymentStatus("EXCESS");
        }
    }

    private Optional<PaymentItemModel> findPaymentItemById(PaymentModel paymentModel, String paymentId) {
        return paymentModel.getPaymentItems().stream()
                .filter(paymentItem -> paymentItem.getPaymentId().equals(paymentId))
                .findFirst();
    }
}
