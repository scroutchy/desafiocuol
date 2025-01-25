package br.com.desafio.controller;

import br.com.desafio.domain.config.PaymentSqsClient;
import br.com.desafio.domain.model.api.PaymentApiDto;
import br.com.desafio.domain.model.api.PaymentItemApiDto;
import br.com.desafio.domain.model.entity.Payment;
import br.com.desafio.domain.model.entity.PaymentItem;
import br.com.desafio.repository.PaymentRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.containers.localstack.LocalStackContainer;
import org.testcontainers.utility.DockerImageName;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.CreateQueueRequest;

import java.math.BigDecimal;
import java.util.Collections;

import static java.util.Collections.singletonList;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.testcontainers.containers.localstack.LocalStackContainer.Service.SQS;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class PaymentControllerIntegrationTest {

    static MongoDBContainer mongoDBContainer = new MongoDBContainer("mongo:5.0.3");

    static {
        mongoDBContainer.start();
        System.setProperty("spring.data.mongodb.uri", mongoDBContainer.getReplicaSetUrl());
    }

    static LocalStackContainer localStackContainer = new LocalStackContainer(DockerImageName.parse("localstack/localstack:1.4.0"))
            .withServices(SQS);

    static {
        localStackContainer.start();
        System.setProperty("AWS_ACCESS_KEY_ID", "test");
        System.setProperty("AWS_SECRET_ACCESS_KEY", "test");
        System.setProperty("AWS_REGION", "us-east-1");
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private PaymentSqsClient paymentSqsClient;

    @BeforeEach
    public void setup() {
        paymentRepository.deleteAll();

        SqsClient localSqsClient = paymentSqsClient.getSqsClient();
        String partialQueueUrl = localSqsClient.createQueue(CreateQueueRequest.builder().queueName("partial-payments").build()).queueUrl();
        String fullQueueUrl = localSqsClient.createQueue(CreateQueueRequest.builder().queueName("full-payments").build()).queueUrl();
        String excessQueueUrl = localSqsClient.createQueue(CreateQueueRequest.builder().queueName("excess-payments").build()).queueUrl();

        Payment payment = Payment.builder()
                .clientId("C001")
                .paymentItems(singletonList(PaymentItem.builder()
                        .paymentId("P001")
                        .paymentValue(new BigDecimal("100.00"))
                        .build()))
                .build();
        paymentRepository.save(payment);

        paymentSqsClient.setPartialPaymentQueueUrl(partialQueueUrl);
        paymentSqsClient.setFullPaymentQueueUrl(fullQueueUrl);
        paymentSqsClient.setExcessPaymentQueueUrl(excessQueueUrl);
    }

    @Test
    void testSetPayments_SuccessForTotal() throws Exception {

        PaymentApiDto paymentRequest = PaymentApiDto.builder()
                .clientId("C001")
                .paymentItems(Collections.singletonList(PaymentItemApiDto.builder()
                        .paymentId("P001")
                        .paymentValue(new BigDecimal("100.00"))
                        .build()))
                .build();

        mockMvc.perform(post("/api/payments")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(paymentRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.client_id").value("C001"))
                .andExpect(jsonPath("$.payment_items[0].payment_id").value("P001"))
                .andExpect(jsonPath("$.payment_items[0].payment_value").value(100.00))
                .andExpect(jsonPath("$.payment_items[0].payment_status").value("TOTAL"));

        var sqsClient = paymentSqsClient.getSqsClient();
        var totalQueueUrl = paymentSqsClient.getFullPaymentQueueUrl();
        var partialQueueUrl = paymentSqsClient.getPartialPaymentQueueUrl();
        var excessQueueUrl = paymentSqsClient.getExcessPaymentQueueUrl();

        assertTrue(sqsClient.receiveMessage(b -> b.queueUrl(totalQueueUrl).maxNumberOfMessages(1)).hasMessages());
        assertFalse(sqsClient.receiveMessage(b -> b.queueUrl(partialQueueUrl).maxNumberOfMessages(1).waitTimeSeconds(1)).hasMessages());
        assertFalse(sqsClient.receiveMessage(b -> b.queueUrl(excessQueueUrl).maxNumberOfMessages(1).waitTimeSeconds(1)).hasMessages());

//        assertFalse(messages.isEmpty());
//        var message = messages.stream().findFirst().get();
//        var messageBody = message.body();
//        System.out.println("Raw SQS Message Body: " + messageBody);
//        var paymentItemSqsDto = objectMapper.readValue(message.body(), PaymentItemSqsDto.class);
//        assertEquals("P001", paymentItemSqsDto.getPaymentId());
//        assertEquals(new BigDecimal("100.00"), paymentItemSqsDto.getPaymentValue());
    }

    @Test
    void testSetPayments_SuccessForExcess() throws Exception {

        PaymentApiDto paymentRequest = PaymentApiDto.builder()
                .clientId("C001")
                .paymentItems(Collections.singletonList(PaymentItemApiDto.builder()
                        .paymentId("P001")
                        .paymentValue(new BigDecimal("150.00"))
                        .build()))
                .build();

        mockMvc.perform(post("/api/payments")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(paymentRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.client_id").value("C001"))
                .andExpect(jsonPath("$.payment_items[0].payment_id").value("P001"))
                .andExpect(jsonPath("$.payment_items[0].payment_value").value(150.00))
                .andExpect(jsonPath("$.payment_items[0].payment_status").value("EXCESS"));

        var sqsClient = paymentSqsClient.getSqsClient();
        var totalQueueUrl = paymentSqsClient.getFullPaymentQueueUrl();
        var partialQueueUrl = paymentSqsClient.getPartialPaymentQueueUrl();
        var excessQueueUrl = paymentSqsClient.getExcessPaymentQueueUrl();

        assertFalse(sqsClient.receiveMessage(b -> b.queueUrl(totalQueueUrl).maxNumberOfMessages(1)).hasMessages());
        assertFalse(sqsClient.receiveMessage(b -> b.queueUrl(partialQueueUrl).maxNumberOfMessages(1)).hasMessages());
        assertTrue(sqsClient.receiveMessage(b -> b.queueUrl(excessQueueUrl).maxNumberOfMessages(1)).hasMessages());
    }

    @Test
    void testSetPayments_SuccessForPartial() throws Exception {

        PaymentApiDto paymentRequest = PaymentApiDto.builder()
                .clientId("C001")
                .paymentItems(Collections.singletonList(PaymentItemApiDto.builder()
                        .paymentId("P001")
                        .paymentValue(new BigDecimal("50.00"))
                        .build()))
                .build();

        mockMvc.perform(post("/api/payments")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(paymentRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.client_id").value("C001"))
                .andExpect(jsonPath("$.payment_items[0].payment_id").value("P001"))
                .andExpect(jsonPath("$.payment_items[0].payment_value").value(50.00))
                .andExpect(jsonPath("$.payment_items[0].payment_status").value("PARTIAL"));


        var sqsClient = paymentSqsClient.getSqsClient();
        var totalQueueUrl = paymentSqsClient.getFullPaymentQueueUrl();
        var partialQueueUrl = paymentSqsClient.getPartialPaymentQueueUrl();
        var excessQueueUrl = paymentSqsClient.getExcessPaymentQueueUrl();

        assertFalse(sqsClient.receiveMessage(b -> b.queueUrl(totalQueueUrl).maxNumberOfMessages(1)).hasMessages());
        assertTrue(sqsClient.receiveMessage(b -> b.queueUrl(partialQueueUrl).maxNumberOfMessages(1)).hasMessages());
        assertFalse(sqsClient.receiveMessage(b -> b.queueUrl(excessQueueUrl).maxNumberOfMessages(1)).hasMessages());
    }

    @Test
    void testSetPayments_FailureForClientNotFound() throws Exception {

        PaymentApiDto paymentRequest = PaymentApiDto.builder()
                .clientId("dummy")
                .paymentItems(Collections.singletonList(PaymentItemApiDto.builder()
                        .paymentId("P001")
                        .paymentValue(new BigDecimal("50.00"))
                        .build()))
                .build();

        mockMvc.perform(post("/api/payments")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(paymentRequest)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("errorMessage").value("Client ID dummy not found."));

        var sqsClient = paymentSqsClient.getSqsClient();
        var totalQueueUrl = paymentSqsClient.getFullPaymentQueueUrl();
        var partialQueueUrl = paymentSqsClient.getPartialPaymentQueueUrl();
        var excessQueueUrl = paymentSqsClient.getExcessPaymentQueueUrl();

        assertFalse(sqsClient.receiveMessage(b -> b.queueUrl(totalQueueUrl).maxNumberOfMessages(1)).hasMessages());
        assertFalse(sqsClient.receiveMessage(b -> b.queueUrl(partialQueueUrl).maxNumberOfMessages(1)).hasMessages());
        assertFalse(sqsClient.receiveMessage(b -> b.queueUrl(excessQueueUrl).maxNumberOfMessages(1)).hasMessages());
    }

    @Test
    void testSetPayments_FailureForPaymentItemNotFound() throws Exception {

        PaymentApiDto paymentRequest = PaymentApiDto.builder()
                .clientId("C001")
                .paymentItems(Collections.singletonList(PaymentItemApiDto.builder()
                        .paymentId("dummy")
                        .paymentValue(new BigDecimal("50.00"))
                        .build()))
                .build();

        mockMvc.perform(post("/api/payments")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(paymentRequest)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("errorMessage").value("Payment ID dummy not found."));

        var sqsClient = paymentSqsClient.getSqsClient();
        var totalQueueUrl = paymentSqsClient.getFullPaymentQueueUrl();
        var partialQueueUrl = paymentSqsClient.getPartialPaymentQueueUrl();
        var excessQueueUrl = paymentSqsClient.getExcessPaymentQueueUrl();

        assertFalse(sqsClient.receiveMessage(b -> b.queueUrl(totalQueueUrl).maxNumberOfMessages(1)).hasMessages());
        assertFalse(sqsClient.receiveMessage(b -> b.queueUrl(partialQueueUrl).maxNumberOfMessages(1)).hasMessages());
        assertFalse(sqsClient.receiveMessage(b -> b.queueUrl(excessQueueUrl).maxNumberOfMessages(1)).hasMessages());
    }
}
