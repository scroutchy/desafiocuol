package br.com.desafio.domain.config;

import br.com.desafio.domain.model.PaymentModel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;

@Component
@RequiredArgsConstructor
@Setter
public class SQSClient {

    @Value("${aws.sqs.partialPaymentQueueUrl}")
    private String partialPaymentQueueUrl;

    @Value("${aws.sqs.fullPaymentQueueUrl}")
    private String fullPaymentQueueUrl;

    @Value("${aws.sqs.excessPaymentQueueUrl}")
    private String excessPaymentQueueUrl;

    @Getter
    private final SqsClient sqsClient;

    public void sendToQueueByPaymentStatus(PaymentModel payment, String paymentStatus) {
        String queueUrl = switch (paymentStatus) {
            case "PARTIAL" -> partialPaymentQueueUrl;
            case "TOTAL" -> fullPaymentQueueUrl;
            case "EXCESS" -> excessPaymentQueueUrl;
            default -> throw new IllegalArgumentException("Status desconhecido");
        };
        sendMessage(queueUrl, payment.toString(), ObjectId.get().toHexString());
    }

    private void sendMessage(String queueUrl, String messageBody, String messageGroupId) {
        SendMessageRequest.Builder requestBuilder = SendMessageRequest.builder()
                .queueUrl(queueUrl)
                .messageBody(messageBody);

        // Add MessageGroupId only if the queue is a FIFO queue
        if (queueUrl.endsWith(".fifo")) {
            requestBuilder.messageGroupId(messageGroupId);
        }

        sqsClient.sendMessage(requestBuilder.build());
    }

// to uncomment for live test , new attempt
//    private void sendMessage(String queueUrl, String messageBody) {
//        System.out.println("Sending message: " + messageBody + " to queueUrl : " + queueUrl);
//    }
}
