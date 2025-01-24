package br.com.desafio.controller;

import br.com.desafio.domain.mapper.PaymentMapper;
import br.com.desafio.domain.model.Payment;
import br.com.desafio.domain.usecase.ConfirmPaymentUseCase;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.http.HttpStatus.OK;

@Slf4j
@RequiredArgsConstructor
@RestController
public class PaymentController {

    private final PaymentMapper paymentMapper;
    private final ConfirmPaymentUseCase confirmPaymentUseCase;

    @PostMapping(path = "/api/payments")
    public ResponseEntity<PaymentApiDto> setPayments(@Valid @RequestBody PaymentApiDto request) {
        log.debug("Received payment request: {}", request);
        Payment payment = paymentMapper.toPayment(request);
        log.debug("Mapped PaymentApiDto to Payment: {}", payment);
        Payment updatedPayment = confirmPaymentUseCase.confirm(payment);
        log.debug("Payment processed successfully: {}", updatedPayment);

        PaymentApiDto responsePayment = paymentMapper.toPaymentApiDto(updatedPayment);
        log.debug("Mapped updated Payment to PaymentApiDto: {}", responsePayment);
        return ResponseEntity.status(OK).body(responsePayment);

    }
}
