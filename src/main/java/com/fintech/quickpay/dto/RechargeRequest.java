package com.fintech.quickpay.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class RechargeRequest {
    private Long bankCardId;

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "1.00", message = "Minimum recharge amount is 1.00")
    private BigDecimal amount;
}
