package com.fintech.quickpay.service;

import com.fintech.quickpay.entity.Account;
import com.fintech.quickpay.entity.RiskRule;
import com.fintech.quickpay.entity.Transaction;
import com.fintech.quickpay.repository.RiskRuleRepository;
import com.fintech.quickpay.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RiskService {
    private final RiskRuleRepository riskRuleRepository;
    private final TransactionRepository transactionRepository;

    public int evaluateTransactionRisk(Account fromAccount, BigDecimal amount, Transaction.TransactionType type) {
        int riskScore = 0;
        List<RiskRule> activeRules = riskRuleRepository.findByEnabledTrue();

        for (RiskRule rule : activeRules) {
            riskScore += evaluateRule(rule, fromAccount, amount, type);
        }

        return Math.min(riskScore, 100);
    }

    private int evaluateRule(RiskRule rule, Account account, BigDecimal amount, Transaction.TransactionType type) {
        switch (rule.getRuleType()) {
            case AMOUNT_DAILY:
                if (amount.compareTo(rule.getThresholdValue()) > 0) {
                    return rule.getRiskScore();
                }
                break;

            case FREQUENCY_LIMIT:
                LocalDateTime startTime = LocalDateTime.now().minusMinutes(rule.getTimeWindowMinutes());
                long transactionCount = transactionRepository.countByFromAccountAndCreatedAtAfter(account, startTime);
                if (transactionCount >= rule.getThresholdValue().longValue()) {
                    return rule.getRiskScore();
                }
                break;

            case DAILY_LIMIT:
                LocalDateTime todayStart = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0);
                BigDecimal dailyTotal = transactionRepository.sumAmountByFromAccountAndCreatedAtAfter(account, todayStart);
                if (dailyTotal == null) dailyTotal = BigDecimal.ZERO;
                if (dailyTotal.add(amount).compareTo(rule.getThresholdValue()) > 0) {
                    return rule.getRiskScore();
                }
                break;

            case NEW_ACCOUNT:
                if (account.getCreatedAt().isAfter(LocalDateTime.now().minusDays(7))) {
                    if (amount.compareTo(rule.getThresholdValue()) > 0) {
                        return rule.getRiskScore();
                    }
                }
                break;

            case BLACKLIST:
                // Blacklist logic would check against a blacklist table
                break;
        }
        return 0;
    }

    public boolean isHighRisk(int riskScore) {
        return riskScore >= 70;
    }

    public boolean requiresManualReview(int riskScore) {
        return riskScore >= 50 && riskScore < 70;
    }
}
