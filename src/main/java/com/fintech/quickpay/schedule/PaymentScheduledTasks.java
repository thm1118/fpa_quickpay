package com.fintech.quickpay.schedule;

import com.fintech.quickpay.entity.Account;
import com.fintech.quickpay.entity.Transaction;
import com.fintech.quickpay.repository.TransactionRepository;
import com.fintech.quickpay.service.AccountService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 支付计划任务：超时交易处理 & 日终对账汇总
 */
@Component
@RequiredArgsConstructor
public class PaymentScheduledTasks {

    private static final Logger log = LoggerFactory.getLogger(PaymentScheduledTasks.class);

    private final TransactionRepository transactionRepository;
    private final AccountService accountService;

    /**
     * 超时交易处理 — 每 5 分钟执行一次。
     * 查找创建时间超过 30 分钟且仍处于 PENDING / PROCESSING 状态的交易，
     * 将其标记为 FAILED，并解冻 fromAccount 上已冻结的金额。
     */
    @Scheduled(fixedRate = 300000)
    @Transactional
    public void handleStaleTransactions() {
        LocalDateTime threshold = LocalDateTime.now().minusMinutes(30);
        log.info("[超时交易处理] 开始扫描，截止时间阈值: {}", threshold);

        List<Transaction> pendingStale =
                transactionRepository.findByStatusAndCreatedAtBefore(
                        Transaction.TransactionStatus.PENDING, threshold);
        List<Transaction> processingStale =
                transactionRepository.findByStatusAndCreatedAtBefore(
                        Transaction.TransactionStatus.PROCESSING, threshold);

        int timeoutCount = 0;
        int unfreezeCount = 0;

        for (Transaction tx : pendingStale) {
            timeoutCount += timeoutTransaction(tx);
            unfreezeCount += tryUnfreezeFromAccount(tx);
        }
        for (Transaction tx : processingStale) {
            timeoutCount += timeoutTransaction(tx);
            unfreezeCount += tryUnfreezeFromAccount(tx);
        }

        log.info("[超时交易处理] 完成。共超时交易: {}，解冻账户次数: {}", timeoutCount, unfreezeCount);
    }

    /**
     * 将单笔交易标记为 FAILED。
     *
     * @return 1（表示处理了 1 笔交易）
     */
    private int timeoutTransaction(Transaction tx) {
        tx.setStatus(Transaction.TransactionStatus.FAILED);
        tx.setFailReason("交易超时自动失败");
        transactionRepository.save(tx);
        log.debug("[超时交易处理] 交易 {} ({}) 已标记为 FAILED", tx.getTransactionNo(), tx.getStatus());
        return 1;
    }

    /**
     * 若交易有 fromAccount 且账户有冻结金额，则尝试解冻该笔交易的金额（含手续费）。
     *
     * @return 1 表示成功解冻，0 表示跳过
     */
    private int tryUnfreezeFromAccount(Transaction tx) {
        Account fromAccount = tx.getFromAccount();
        if (fromAccount == null) {
            return 0;
        }

        // 冻结时锁定的是 amount + fee
        BigDecimal frozenForTx = tx.getAmount().add(
                tx.getFee() != null ? tx.getFee() : BigDecimal.ZERO);

        if (fromAccount.getFrozenAmount().compareTo(frozenForTx) >= 0) {
            try {
                accountService.unfreezeAmount(fromAccount, frozenForTx);
                log.debug("[超时交易处理] 已解冻账户 {} 金额 {}", fromAccount.getAccountNo(), frozenForTx);
                return 1;
            } catch (IllegalStateException e) {
                log.warn("[超时交易处理] 解冻账户 {} 失败: {}", fromAccount.getAccountNo(), e.getMessage());
            }
        }
        return 0;
    }

    /**
     * 日终交易对账汇总 — 每天 23:30 执行。
     * 按状态统计当日交易笔数和总金额，输出 INFO 日志汇总。
     */
    @Scheduled(cron = "0 30 23 * * *")
    public void dailyReconciliationSummary() {
        LocalDateTime dayStart = LocalDate.now().atStartOfDay();
        LocalDateTime dayEnd = dayStart.plusDays(1);

        log.info("[日终对账汇总] 开始统计 {} 全天交易数据", LocalDate.now());

        List<Object[]> rows = transactionRepository.aggregateByStatusForDay(dayStart, dayEnd);

        if (rows.isEmpty()) {
            log.info("[日终对账汇总] {} 全天无交易记录", LocalDate.now());
            return;
        }

        long totalCount = 0;
        BigDecimal totalAmount = BigDecimal.ZERO;

        StringBuilder sb = new StringBuilder();
        sb.append("[日终对账汇总] ").append(LocalDate.now()).append(" 对账汇总:\n");

        for (Object[] row : rows) {
            Transaction.TransactionStatus status = (Transaction.TransactionStatus) row[0];
            long count = ((Number) row[1]).longValue();
            BigDecimal amount = (BigDecimal) row[2];

            sb.append(String.format("  %-12s 笔数: %4d  总金额: %,.2f%n",
                    status.name(), count, amount));

            totalCount += count;
            totalAmount = totalAmount.add(amount);
        }

        sb.append(String.format("  %-12s 笔数: %4d  总金额: %,.2f", "【合计】", totalCount, totalAmount));
        log.info("{}", sb);
    }
}
