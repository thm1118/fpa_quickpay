package com.fintech.quickpay.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "transactions")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Transaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 32)
    private String transactionNo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "from_account_id")
    private Account fromAccount;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "to_account_id")
    private Account toAccount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TransactionType type;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;

    @Column(precision = 15, scale = 2)
    private BigDecimal fee = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TransactionStatus status = TransactionStatus.PENDING;

    @Column(length = 200)
    private String description;

    @Column(length = 500)
    private String remark;

    @Column
    private Integer riskScore = 0;

    @Column(length = 200)
    private String riskReason;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @Column
    private LocalDateTime completedAt;
    private String failReason;

    public enum TransactionType {
        TRANSFER,       // 转账
        RECHARGE,       // 充值
        WITHDRAW,       // 提现
        PAYMENT,        // 支付
        REFUND          // 退款
    }

    public enum TransactionStatus {
        PENDING,        // 待处理
        PROCESSING,     // 处理中
        SUCCESS,        // 成功
        FAILED,         // 失败
        CANCELLED,      // 已取消
        REFUNDED,       // 已退款
        RISK_REVIEW     // 风险审核
    }
}
