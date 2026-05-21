package com.fintech.quickpay.repository;

import com.fintech.quickpay.dto.TransactionDTO;
import com.fintech.quickpay.entity.Account;
import com.fintech.quickpay.entity.Transaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    Optional<Transaction> findByTransactionNo(String transactionNo);

    @Query("SELECT t FROM Transaction t WHERE t.fromAccount = :account OR t.toAccount = :account ORDER BY t.createdAt DESC")
    Page<Transaction> findByAccount(@Param("account") Account account, Pageable pageable);

    @Query("SELECT COUNT(t) FROM Transaction t WHERE t.fromAccount = :account AND t.createdAt > :startTime AND t.status = 'SUCCESS'")
    Long countByAccountAndPeriod(@Param("account") Account account, @Param("startTime") LocalDateTime startTime);

    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t WHERE t.fromAccount = :account AND t.createdAt > :startTime AND t.status = 'SUCCESS'")
    BigDecimal sumAmountByAccountAndPeriod(@Param("account") Account account, @Param("startTime") LocalDateTime startTime);

    List<Transaction> findByStatusAndCreatedAtBefore(Transaction.TransactionStatus status, LocalDateTime time);

    Page<TransactionDTO> findByFromAccountOrToAccountOrderByCreatedAtDesc(Account account, Account account1, Pageable pageable);

    long countByFromAccountAndCreatedAtAfter(Account account, LocalDateTime startTime);

    BigDecimal sumAmountByFromAccountAndCreatedAtAfter( Account account, LocalDateTime todayStart);

    @Query("SELECT t.status, COUNT(t), COALESCE(SUM(t.amount), 0) FROM Transaction t " +
           "WHERE t.createdAt >= :dayStart AND t.createdAt < :dayEnd " +
           "GROUP BY t.status")
    List<Object[]> aggregateByStatusForDay(@Param("dayStart") LocalDateTime dayStart,
                                           @Param("dayEnd") LocalDateTime dayEnd);
}
