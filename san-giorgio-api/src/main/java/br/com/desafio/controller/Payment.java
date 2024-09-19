package br.com.desafio.controller;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Payment {
    @JsonProperty("client_id")
    private String clientId;
    @JsonProperty("payment_items")
    private List<PaymentItem> paymentItems;
    @JsonProperty("error_message")
    private String errorMessage;
}
