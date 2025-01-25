package br.com.desafio.domain.model.sqs;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PaymentItemSqsDto {
    private String paymentId;
    private BigDecimal paymentValue;
}
