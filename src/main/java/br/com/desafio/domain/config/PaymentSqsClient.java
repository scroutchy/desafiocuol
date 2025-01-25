package br.com.desafio.domain.config;

import br.com.desafio.domain.mapper.PaymentMapper;
import br.com.desafio.domain.model.entity.PaymentItem;
import br.com.desafio.domain.model.sqs.PaymentItemSqsDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;

@Component
@RequiredArgsConstructor
@Data
public class PaymentSqsClient {

    @Value("${aws.sqs.partialPaymentQueueUrl}")
    private String partialPaymentQueueUrl;

    @Value("${aws.sqs.fullPaymentQueueUrl}")
    private String fullPaymentQueueUrl;

    @Value("${aws.sqs.excessPaymentQueueUrl}")
    private String excessPaymentQueueUrl;

    @Getter
    private final SqsClient sqsClient;

    private final PaymentMapper paymentMapper;

    private final ObjectMapper objectMapper;

    public void sendToQueueByPaymentStatus(PaymentItem paymentItem, String paymentStatus) {
        String queueUrl = switch (paymentStatus) {
            case "PARTIAL" -> partialPaymentQueueUrl;
            case "TOTAL" -> fullPaymentQueueUrl;
            case "EXCESS" -> excessPaymentQueueUrl;
            default -> throw new IllegalArgumentException("Status desconhecido");
        };
        sendMessage(queueUrl, paymentMapper.toPaymentItemSqsDto(paymentItem), ObjectId.get().toHexString());
    }

    @SneakyThrows
    private void sendMessage(String queueUrl, PaymentItemSqsDto messageBody, String messageGroupId) {
        String jsonMessage = objectMapper.writeValueAsString(messageBody);

        SendMessageRequest.Builder requestBuilder = SendMessageRequest.builder()
                .queueUrl(queueUrl)
                .messageBody(jsonMessage);

        // Add MessageGroupId only if the queue is a FIFO queue
        if (queueUrl.endsWith(".fifo")) {
            requestBuilder.messageGroupId(messageGroupId);
        }

        sqsClient.sendMessage(requestBuilder.build());
    }
}
