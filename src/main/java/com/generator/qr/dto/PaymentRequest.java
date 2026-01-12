package com.generator.qr.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class PaymentRequest {

    private String upiId;
    private BigDecimal amount;

}
