package com.fintech.quickpay.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(name = "risk_rules")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RiskRule {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String ruleCode;

    @Column(nullable = false, length = 100)
    private String ruleName;

    @Column(length = 500)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private RuleType ruleType;

    @Column(precision = 15, scale = 2)
    private BigDecimal threshold;

    @Column(nullable = false)
    private Integer riskScore = 0;

    @Column(nullable = false)
    private Boolean enabled = true;
    private BigDecimal thresholdValue;
    private long timeWindowMinutes;

    public enum RuleType {
        AMOUNT_SINGLE,      // 单笔金额
        AMOUNT_DAILY,       // 每日累计金额
        FREQUENCY_HOURLY,   // 每小时交易频率
        FREQUENCY_DAILY,    // 每日交易频率
        TIME_WINDOW,        // 时间窗口（如深夜交易）
        FREQUENCY_LIMIT, DAILY_LIMIT, BLACKLIST, NEW_ACCOUNT         // 新账户交易
    }
}
