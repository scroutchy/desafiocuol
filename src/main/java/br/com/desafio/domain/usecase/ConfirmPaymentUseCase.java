package br.com.desafio.domain.usecase;


import br.com.desafio.domain.model.entity.Payment;

public interface ConfirmPaymentUseCase {
    Payment confirm(Payment payment);
}
