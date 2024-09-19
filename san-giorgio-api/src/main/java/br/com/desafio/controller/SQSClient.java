package br.com.desafio.controller;

import br.com.desafio.domain.model.PaymentModel;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;

@Component
@RequiredArgsConstructor
public class SQSClient {

    private static final String PARTIAL_PAYMENT_QUEUE_URL = "https://sqs.us-east-1.amazonaws.com/123456789012/partial-payments";
    private static final String FULL_PAYMENT_QUEUE_URL = "https://sqs.us-east-1.amazonaws.com/123456789012/full-payments";
    private static final String EXCESS_PAYMENT_QUEUE_URL = "https://sqs.us-east-1.amazonaws.com/123456789012/excess-payments";
    private final SqsClient sqsClient;

    public void sendToQueueByPaymentStatus(PaymentModel payment, String paymentStatus) {
        String queueUrl = switch (paymentStatus) {
            case "PARTIAL" -> PARTIAL_PAYMENT_QUEUE_URL;
            case "TOTAL" -> FULL_PAYMENT_QUEUE_URL;
            case "EXCESS" -> EXCESS_PAYMENT_QUEUE_URL;
            default -> throw new IllegalArgumentException("Status desconhecido");
        };
        sendMessage(queueUrl, payment.toString());
    }

    private void sendMessage(String queueUrl, String messageBody) {
        SendMessageRequest request = SendMessageRequest.builder()
                .queueUrl(queueUrl)
                .messageBody(messageBody)
                .build();
        sqsClient.sendMessage(request);
//        temporary
//        System.out.println("Mock SQS: Sending message to " + queueUrl);
    }
}
