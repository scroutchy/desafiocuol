package br.com.desafio.controller;

import br.com.desafio.domain.mapper.PaymentMapper;
import br.com.desafio.domain.model.PaymentModel;
import br.com.desafio.domain.usecase.ConfirmPaymentUseCase;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.http.HttpStatus.OK;

@RequiredArgsConstructor
@RestController
public class PaymentController {

    private final PaymentMapper paymentMapper;
    private final ConfirmPaymentUseCase confirmPaymentUseCase;

    @PostMapping(path = "/api/payments")
    public ResponseEntity<Payment> setPayments(@Valid @RequestBody Payment request) {
        PaymentModel paymentModel = paymentMapper.toPaymentModel(request);
        PaymentModel updatedPaymentModel = confirmPaymentUseCase.confirm(paymentModel);

        Payment responsePayment = paymentMapper.toPayment(updatedPaymentModel);
        return ResponseEntity.status(OK).body(responsePayment);

    }
}
