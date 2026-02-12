package com.fintech.quickpay.service;

import com.fintech.quickpay.dto.AccountDTO;
import com.fintech.quickpay.entity.Account;
import com.fintech.quickpay.entity.User;
import com.fintech.quickpay.exception.ResourceNotFoundException;
import com.fintech.quickpay.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class AccountService {
    private final AccountRepository accountRepository;

    public Account getAccountByUser(User user) {
        return accountRepository.findByUser(user)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found"));
    }

    public Account getAccountByAccountNo(String accountNo) {
        return accountRepository.findByAccountNo(accountNo)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found: " + accountNo));
    }

    @Transactional
    public void addBalance(Account account, BigDecimal amount) {
        account.setBalance(account.getBalance().add(amount));
        accountRepository.save(account);
    }

    @Transactional
    public void subtractBalance(Account account, BigDecimal amount) {
        BigDecimal available = account.getBalance().subtract(account.getFrozenAmount());
        if (available.compareTo(amount) < 0) {
            throw new IllegalStateException("Insufficient balance");
        }
        account.setBalance(account.getBalance().subtract(amount));
        accountRepository.save(account);
    }

    @Transactional
    public void freezeAmount(Account account, BigDecimal amount) {
        BigDecimal available = account.getBalance().subtract(account.getFrozenAmount());
        if (available.compareTo(amount) < 0) {
            throw new IllegalStateException("Insufficient available balance to freeze");
        }
        account.setFrozenAmount(account.getFrozenAmount().add(amount));
        accountRepository.save(account);
    }

    @Transactional
    public void unfreezeAmount(Account account, BigDecimal amount) {
        if (account.getFrozenAmount().compareTo(amount) < 0) {
            throw new IllegalStateException("Cannot unfreeze more than frozen amount");
        }
        account.setFrozenAmount(account.getFrozenAmount().subtract(amount));
        accountRepository.save(account);
    }

    @Transactional
    public void updateDailyLimit(Account account, BigDecimal limit) {
        account.setDailyLimit(limit);
        accountRepository.save(account);
    }

    public AccountDTO convertToDTO(Account account) {
        AccountDTO dto = new AccountDTO();
        dto.setId(account.getId());
        dto.setAccountNo(account.getAccountNo());
        dto.setBalance(account.getBalance());
        dto.setFrozenAmount(account.getFrozenAmount());
        dto.setAvailableBalance(account.getBalance().subtract(account.getFrozenAmount()));
        dto.setDailyLimit(account.getDailyLimit());
        dto.setStatus(account.getStatus());
        return dto;
    }
}
