package com.fintech.quickpay.dto;

import com.fintech.quickpay.entity.Account;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class AccountDTO {
    private Long id;
    private String accountNo;
    private BigDecimal balance;
    private BigDecimal availableBalance;
    private BigDecimal frozenAmount;
    private Account.AccountStatus status;
    private BigDecimal dailyLimit;
    private BigDecimal singleLimit;
}
