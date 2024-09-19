package br.com.desafio.controller;

import br.com.desafio.domain.mapper.PaymentMapper;
import br.com.desafio.domain.model.PaymentModel;
import br.com.desafio.domain.usecase.ConfirmPaymentUseCase;
import br.com.desafio.exception.ClientNotFoundException;
import br.com.desafio.exception.PaymentItemNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.http.HttpStatus.*;

@RequiredArgsConstructor
@RestController
public class PaymentController {

    private final PaymentMapper paymentMapper;
    private final ConfirmPaymentUseCase confirmPaymentUseCase;

    @PostMapping(path = "/api/payment")
    public ResponseEntity<?> setPayment(@Valid @RequestBody Payment request) {
        Payment responsePayment;

        try {
            PaymentModel paymentModel = paymentMapper.toPaymentModel(request);

            PaymentModel updatedPaymentModel = confirmPaymentUseCase.confirm(paymentModel);

            responsePayment = paymentMapper.toPayment(updatedPaymentModel);

            return ResponseEntity.status(OK).body(responsePayment);
        } catch (ClientNotFoundException | PaymentItemNotFoundException e) {
            return ResponseEntity.status(NOT_FOUND).body(new ErrorResponse(e.getMessage()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(BAD_REQUEST).body(new ErrorResponse(e.getMessage()));
        }

    }
}
