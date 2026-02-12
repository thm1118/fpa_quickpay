package com.fintech.quickpay.repository;

import com.fintech.quickpay.entity.BankCard;
import com.fintech.quickpay.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BankCardRepository extends JpaRepository<BankCard, Long> {
    List<BankCard> findByUserAndActiveTrue(User user);

    Optional<BankCard> findByUserAndIsDefaultTrue(User user);

    Optional<BankCard> findByCardNo(String cardNo);

    boolean existsByCardNo(String cardNo);

    int countByUser(User user);

    List<BankCard> findByUser(User user);
}
