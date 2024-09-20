package br.com.desafio.controller;

import br.com.desafio.domain.config.SQSClient;
import br.com.desafio.domain.model.PaymentItemModel;
import br.com.desafio.domain.model.PaymentModel;
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
    private SQSClient sqsClient;

    @BeforeEach
    public void setup() {
        paymentRepository.deleteAll();

        SqsClient localSqsClient = sqsClient.getSqsClient();
        String partialQueueUrl = localSqsClient.createQueue(CreateQueueRequest.builder().queueName("partial-payments").build()).queueUrl();
        String fullQueueUrl = localSqsClient.createQueue(CreateQueueRequest.builder().queueName("full-payments").build()).queueUrl();
        String excessQueueUrl = localSqsClient.createQueue(CreateQueueRequest.builder().queueName("excess-payments").build()).queueUrl();

        PaymentModel payment = PaymentModel.builder()
                .clientId("C001")
                .paymentItems(singletonList(PaymentItemModel.builder()
                        .paymentId("P001")
                        .paymentValue(new BigDecimal("100.00"))
                        .build()))
                .build();
        paymentRepository.save(payment);

        sqsClient.setPartialPaymentQueueUrl(partialQueueUrl);
        sqsClient.setFullPaymentQueueUrl(fullQueueUrl);
        sqsClient.setExcessPaymentQueueUrl(excessQueueUrl);
    }

    @Test
    void testSetPayments_SuccessForTotal() throws Exception {

        Payment paymentRequest = Payment.builder()
                .clientId("C001")
                .paymentItems(Collections.singletonList(PaymentItem.builder()
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
    }

    @Test
    void testSetPayments_SuccessForExcess() throws Exception {

        Payment paymentRequest = Payment.builder()
                .clientId("C001")
                .paymentItems(Collections.singletonList(PaymentItem.builder()
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
    }

    @Test
    void testSetPayments_SuccessForPartial() throws Exception {

        Payment paymentRequest = Payment.builder()
                .clientId("C001")
                .paymentItems(Collections.singletonList(PaymentItem.builder()
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
    }

    @Test
    void testSetPayments_FailureForClientNotFound() throws Exception {

        Payment paymentRequest = Payment.builder()
                .clientId("dummy")
                .paymentItems(Collections.singletonList(PaymentItem.builder()
                        .paymentId("P001")
                        .paymentValue(new BigDecimal("50.00"))
                        .build()))
                .build();

        mockMvc.perform(post("/api/payments")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(paymentRequest)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("errorMessage").value("Client ID dummy not found."));
    }

    @Test
    void testSetPayments_FailureForPaymentItemNotFound() throws Exception {

        Payment paymentRequest = Payment.builder()
                .clientId("C001")
                .paymentItems(Collections.singletonList(PaymentItem.builder()
                        .paymentId("dummy")
                        .paymentValue(new BigDecimal("50.00"))
                        .build()))
                .build();

        mockMvc.perform(post("/api/payments")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(paymentRequest)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("errorMessage").value("Payment ID dummy not found."));
    }
}
