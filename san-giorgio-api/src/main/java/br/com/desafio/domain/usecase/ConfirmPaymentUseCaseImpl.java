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
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ConfirmPaymentUseCaseImpl implements ConfirmPaymentUseCase {

    private static final List<String> VALID_CLIENT_IDS = Arrays.asList("C001", "C002", "C003");
    private static final List<PaymentItemModel> MOCKED_PAYMENT_ITEMS_DB = Arrays.asList(
            PaymentItemModel.builder().paymentId("P001").paymentValue(new BigDecimal("100.00")).build(),
            PaymentItemModel.builder().paymentId("P002").paymentValue(new BigDecimal("200.00")).build(),
            PaymentItemModel.builder().paymentId("P003").paymentValue(new BigDecimal("150.00")).build()
    );
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

            if (receivedValue.compareTo(originalValue) < 0) {
                item.setPaymentStatus("PARTIAL");
            } else if (receivedValue.compareTo(originalValue) == 0) {
                item.setPaymentStatus("TOTAL");
            } else {
                item.setPaymentStatus("EXCESS");
            }

            sqsClient.sendToQueueByPaymentStatus(paymentModel, item.getPaymentStatus());
        }

        return paymentModel;
    }

    private Optional<PaymentItemModel> findPaymentItemById(PaymentModel paymentModel, String paymentId) {
        return paymentModel.getPaymentItems().stream()
                .filter(paymentItem -> paymentItem.getPaymentId().equals(paymentId))
                .findFirst();
    }
}
