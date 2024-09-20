package br.com.desafio.controller;

import br.com.desafio.domain.model.PaymentModel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
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
