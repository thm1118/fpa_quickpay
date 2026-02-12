package com.fintech.quickpay.service;

import com.fintech.quickpay.dto.RechargeRequest;
import com.fintech.quickpay.dto.TransactionDTO;
import com.fintech.quickpay.dto.TransferRequest;
import com.fintech.quickpay.entity.Account;
import com.fintech.quickpay.entity.Transaction;
import com.fintech.quickpay.entity.User;
import com.fintech.quickpay.exception.BusinessException;
import com.fintech.quickpay.exception.ResourceNotFoundException;
import com.fintech.quickpay.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TransactionService {
    private final TransactionRepository transactionRepository;
    private final AccountService accountService;
    private final UserService userService;
    private final RiskService riskService;
    private final NotificationService notificationService;

    private static final BigDecimal TRANSFER_FEE_RATE = new BigDecimal("0.001"); // 0.1%
    private static final BigDecimal MIN_FEE = new BigDecimal("0.01");
    private static final BigDecimal MAX_FEE = new BigDecimal("50.00");

    @Transactional
    public TransactionDTO transfer(User user, TransferRequest request) {
        // Verify payment password
        if (!userService.verifyPaymentPassword(user, request.getPaymentPassword())) {
            notificationService.sendSecurityAlert(user, "Security Alert",
                    "Failed payment password attempt detected");
            throw new BusinessException("Invalid payment password");
        }

        Account fromAccount = accountService.getAccountByUser(user);
        Account toAccount = accountService.getAccountByAccountNo(request.getToAccountNo());

        // Cannot transfer to self
        if (fromAccount.getId().equals(toAccount.getId())) {
            throw new BusinessException("Cannot transfer to your own account");
        }

        // Check account status
        if (fromAccount.getStatus() != Account.AccountStatus.ACTIVE) {
            throw new BusinessException("Your account is not active");
        }
        if (toAccount.getStatus() != Account.AccountStatus.ACTIVE) {
            throw new BusinessException("Target account is not active");
        }

        BigDecimal amount = request.getAmount();
        BigDecimal fee = calculateFee(amount);
        BigDecimal totalAmount = amount.add(fee);

        // Check balance
        BigDecimal available = fromAccount.getBalance().subtract(fromAccount.getFrozenAmount());
        if (available.compareTo(totalAmount) < 0) {
            throw new BusinessException("Insufficient balance");
        }

        // Risk evaluation
        int riskScore = riskService.evaluateTransactionRisk(fromAccount, amount, Transaction.TransactionType.TRANSFER);

        // Create transaction record
        Transaction transaction = new Transaction();
        transaction.setTransactionNo(generateTransactionNo());
        transaction.setFromAccount(fromAccount);
        transaction.setToAccount(toAccount);
        transaction.setType(Transaction.TransactionType.TRANSFER);
        transaction.setAmount(amount);
        transaction.setFee(fee);
        transaction.setDescription(request.getDescription());
        transaction.setRiskScore(riskScore);

        if (riskService.isHighRisk(riskScore)) {
            transaction.setStatus(Transaction.TransactionStatus.FAILED);
            transaction.setFailReason("Transaction blocked due to high risk score");
            transactionRepository.save(transaction);
            notificationService.sendSecurityAlert(user, "Transaction Blocked",
                    "Your transfer was blocked due to security concerns");
            throw new BusinessException("Transaction blocked due to security concerns");
        }

        if (riskService.requiresManualReview(riskScore)) {
            transaction.setStatus(Transaction.TransactionStatus.PENDING);
            transactionRepository.save(transaction);
            accountService.freezeAmount(fromAccount, totalAmount);
            notificationService.sendSystemNotification(user, "Transaction Pending",
                    "Your transfer requires manual review and will be processed within 24 hours");
            return convertToDTO(transaction);
        }

        // Execute transfer
        accountService.subtractBalance(fromAccount, totalAmount);
        accountService.addBalance(toAccount, amount);

        transaction.setStatus(Transaction.TransactionStatus.SUCCESS);
        transaction.setCompletedAt(LocalDateTime.now());
        transactionRepository.save(transaction);

        // Send notifications
        notificationService.sendTransactionNotification(user, transaction, false);
        notificationService.sendTransactionNotification(toAccount.getUser(), transaction, true);

        return convertToDTO(transaction);
    }

    @Transactional
    public TransactionDTO recharge(User user, RechargeRequest request) {
        Account account = accountService.getAccountByUser(user);

        if (account.getStatus() != Account.AccountStatus.ACTIVE) {
            throw new BusinessException("Your account is not active");
        }

        Transaction transaction = new Transaction();
        transaction.setTransactionNo(generateTransactionNo());
        transaction.setToAccount(account);
        transaction.setType(Transaction.TransactionType.RECHARGE);
        transaction.setAmount(request.getAmount());
        transaction.setFee(BigDecimal.ZERO);
        transaction.setDescription("Account recharge");
        transaction.setRiskScore(0);

        // Simulate bank card payment (in real scenario, would integrate with payment gateway)
        accountService.addBalance(account, request.getAmount());

        transaction.setStatus(Transaction.TransactionStatus.SUCCESS);
        transaction.setCompletedAt(LocalDateTime.now());
        transactionRepository.save(transaction);

        notificationService.sendSystemNotification(user, "Recharge Successful",
                String.format("Successfully recharged %.2f to your account", request.getAmount()));

        return convertToDTO(transaction);
    }

    @Transactional
    public TransactionDTO withdraw(User user, BigDecimal amount, String paymentPassword) {
        if (!userService.verifyPaymentPassword(user, paymentPassword)) {
            throw new BusinessException("Invalid payment password");
        }

        Account account = accountService.getAccountByUser(user);

        if (account.getStatus() != Account.AccountStatus.ACTIVE) {
            throw new BusinessException("Your account is not active");
        }

        BigDecimal fee = calculateFee(amount);
        BigDecimal totalAmount = amount.add(fee);

        BigDecimal available = account.getBalance().subtract(account.getFrozenAmount());
        if (available.compareTo(totalAmount) < 0) {
            throw new BusinessException("Insufficient balance");
        }

        int riskScore = riskService.evaluateTransactionRisk(account, amount, Transaction.TransactionType.WITHDRAW);

        Transaction transaction = new Transaction();
        transaction.setTransactionNo(generateTransactionNo());
        transaction.setFromAccount(account);
        transaction.setType(Transaction.TransactionType.WITHDRAW);
        transaction.setAmount(amount);
        transaction.setFee(fee);
        transaction.setDescription("Withdrawal to bank card");
        transaction.setRiskScore(riskScore);

        if (riskService.isHighRisk(riskScore)) {
            transaction.setStatus(Transaction.TransactionStatus.FAILED);
            transaction.setFailReason("Withdrawal blocked due to high risk");
            transactionRepository.save(transaction);
            throw new BusinessException("Withdrawal blocked due to security concerns");
        }

        // Execute withdrawal
        accountService.subtractBalance(account, totalAmount);

        transaction.setStatus(Transaction.TransactionStatus.SUCCESS);
        transaction.setCompletedAt(LocalDateTime.now());
        transactionRepository.save(transaction);

        notificationService.sendSystemNotification(user, "Withdrawal Successful",
                String.format("Successfully withdrew %.2f from your account", amount));

        return convertToDTO(transaction);
    }

    public Transaction getTransaction(String transactionNo) {
        return transactionRepository.findByTransactionNo(transactionNo)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found"));
    }

    public  Page<TransactionDTO> getUserTransactions(User user, Pageable pageable) {
        Account account = accountService.getAccountByUser(user);
        return transactionRepository.findByFromAccountOrToAccountOrderByCreatedAtDesc(
                account, account, pageable);
        //.map(this::convertToDTO)
    }

    private BigDecimal calculateFee(BigDecimal amount) {
        BigDecimal fee = amount.multiply(TRANSFER_FEE_RATE).setScale(2, RoundingMode.HALF_UP);
        if (fee.compareTo(MIN_FEE) < 0) {
            fee = MIN_FEE;
        }
        if (fee.compareTo(MAX_FEE) > 0) {
            fee = MAX_FEE;
        }
        return fee;
    }

    private String generateTransactionNo() {
        return "TXN" + System.currentTimeMillis() + UUID.randomUUID().toString().substring(0, 6).toUpperCase();
    }

    public TransactionDTO convertToDTO(Transaction transaction) {
        TransactionDTO dto = new TransactionDTO();
        dto.setId(transaction.getId());
        dto.setTransactionNo(transaction.getTransactionNo());
        if (transaction.getFromAccount() != null) {
            dto.setFromAccountNo(transaction.getFromAccount().getAccountNo());
        }
        if (transaction.getToAccount() != null) {
            dto.setToAccountNo(transaction.getToAccount().getAccountNo());
        }
        dto.setType(transaction.getType());
        dto.setAmount(transaction.getAmount());
        dto.setFee(transaction.getFee());
        dto.setStatus(transaction.getStatus());
        dto.setDescription(transaction.getDescription());
        dto.setRiskScore(transaction.getRiskScore());
        dto.setCreatedAt(transaction.getCreatedAt());
        dto.setCompletedAt(transaction.getCompletedAt());
        return dto;
    }
}
