package com.fintech.quickpay.dto;

import com.fintech.quickpay.entity.Transaction;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class TransactionDTO {
    private Long id;
    private String transactionNo;
    private String fromAccountNo;
    private String toAccountNo;
    private Transaction.TransactionType type;
    private BigDecimal amount;
    private BigDecimal fee;
    private Transaction.TransactionStatus status;
    private String description;
    private Integer riskScore;
    private LocalDateTime createdAt;
    private LocalDateTime completedAt;
}
