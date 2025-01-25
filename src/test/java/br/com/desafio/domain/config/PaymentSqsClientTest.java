package br.com.desafio.domain.config;

import br.com.desafio.domain.mapper.PaymentMapper;
import br.com.desafio.domain.model.entity.PaymentItem;
import br.com.desafio.domain.model.sqs.PaymentItemSqsDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class PaymentSqsClientTest {

    @Mock
    private SqsClient sqsClient;

    @Mock
    private PaymentMapper paymentMapper;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private PaymentSqsClient paymentSqsClient;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        paymentSqsClient.setPartialPaymentQueueUrl("http://localhost:4566/000000000000/partial-payments");
        paymentSqsClient.setFullPaymentQueueUrl("http://localhost:4566/000000000000/full-payments");
        paymentSqsClient.setExcessPaymentQueueUrl("http://localhost:4566/000000000000/excess-payments");
    }

    @Test
    void testSendToQueueByPaymentStatus_partialPayment() throws Exception {
        PaymentItem paymentItem = new PaymentItem("P001", new BigDecimal("50.00"), null);
        PaymentItemSqsDto paymentItemSqsDto = new PaymentItemSqsDto("P001", new BigDecimal("50.00"));
        when(paymentMapper.toPaymentItemSqsDto(paymentItem)).thenReturn(paymentItemSqsDto);

        ArgumentCaptor<SendMessageRequest> requestCaptor = ArgumentCaptor.forClass(SendMessageRequest.class);

        paymentSqsClient.sendToQueueByPaymentStatus(paymentItem, "PARTIAL");

        verify(sqsClient, times(1)).sendMessage(requestCaptor.capture());
        SendMessageRequest capturedRequest = requestCaptor.getValue();

        assertEquals("http://localhost:4566/000000000000/partial-payments", capturedRequest.queueUrl());

        // Compare the JSON serialized message body with the expected JSON string
        String expectedJsonMessage = objectMapper.writeValueAsString(paymentItemSqsDto);
        assertEquals(expectedJsonMessage, capturedRequest.messageBody());
    }

    @Test
    void testSendToQueueByPaymentStatus_fullPayment() throws Exception {
        PaymentItem paymentItem = new PaymentItem("P002", new BigDecimal("100.00"), null);
        PaymentItemSqsDto paymentItemSqsDto = new PaymentItemSqsDto("P002", new BigDecimal("100.00"));
        when(paymentMapper.toPaymentItemSqsDto(paymentItem)).thenReturn(paymentItemSqsDto);

        ArgumentCaptor<SendMessageRequest> requestCaptor = ArgumentCaptor.forClass(SendMessageRequest.class);

        paymentSqsClient.sendToQueueByPaymentStatus(paymentItem, "TOTAL");

        verify(sqsClient, times(1)).sendMessage(requestCaptor.capture());
        SendMessageRequest capturedRequest = requestCaptor.getValue();

        // Ensure correct queue URL
        assertEquals("http://localhost:4566/000000000000/full-payments", capturedRequest.queueUrl());

        // Compare the JSON serialized message body with the expected JSON string
        String expectedJsonMessage = objectMapper.writeValueAsString(paymentItemSqsDto);
        assertEquals(expectedJsonMessage, capturedRequest.messageBody());
    }


    @Test
    void testSendToQueueByPaymentStatus_excessPayment() throws Exception {
        PaymentItem paymentItem = new PaymentItem("P003", new BigDecimal("150.00"), null);
        PaymentItemSqsDto paymentItemSqsDto = new PaymentItemSqsDto("P003", new BigDecimal("150.00"));
        when(paymentMapper.toPaymentItemSqsDto(paymentItem)).thenReturn(paymentItemSqsDto);

        ArgumentCaptor<SendMessageRequest> requestCaptor = ArgumentCaptor.forClass(SendMessageRequest.class);

        paymentSqsClient.sendToQueueByPaymentStatus(paymentItem, "EXCESS");

        verify(sqsClient, times(1)).sendMessage(requestCaptor.capture());
        SendMessageRequest capturedRequest = requestCaptor.getValue();

        assertEquals("http://localhost:4566/000000000000/excess-payments", capturedRequest.queueUrl());

        // Compare the JSON serialized message body with the expected JSON string
        String expectedJsonMessage = objectMapper.writeValueAsString(paymentItemSqsDto);
        assertEquals(expectedJsonMessage, capturedRequest.messageBody());
    }

    @Test
    void testSendToQueueByPaymentStatus_unknownStatus() {
        PaymentItem paymentItem = new PaymentItem("P004", new BigDecimal("200.00"), null);

        IllegalArgumentException exception =
                assertThrows(IllegalArgumentException.class,
                        () -> paymentSqsClient.sendToQueueByPaymentStatus(paymentItem, "UNKNOWN"));

        assertEquals("Status desconhecido", exception.getMessage());
        verifyNoInteractions(sqsClient); // Ensure SQS is not called
    }
}
