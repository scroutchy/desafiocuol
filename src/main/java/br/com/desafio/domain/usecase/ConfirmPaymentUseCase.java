package br.com.desafio.domain.usecase;


import br.com.desafio.domain.model.Payment;

public interface ConfirmPaymentUseCase {
    Payment confirm(Payment payment);
}
