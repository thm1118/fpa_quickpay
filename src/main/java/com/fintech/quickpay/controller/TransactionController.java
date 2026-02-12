package com.fintech.quickpay.controller;

import com.fintech.quickpay.dto.TransactionDTO;
import com.fintech.quickpay.entity.Transaction;
import com.fintech.quickpay.security.CurrentUser;
import com.fintech.quickpay.security.UserPrincipal;
import com.fintech.quickpay.service.TransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
public class TransactionController {
    private final TransactionService transactionService;

    @GetMapping
    public ResponseEntity<Page<TransactionDTO>> getMyTransactions(
            @CurrentUser UserPrincipal principal,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<TransactionDTO> transactions = transactionService.getUserTransactions(principal.getUser(), pageable);
        return ResponseEntity.ok(transactions);
    }

    @GetMapping("/{transactionNo}")
    public ResponseEntity<TransactionDTO> getTransaction(@PathVariable String transactionNo) {
        Transaction transaction = transactionService.getTransaction(transactionNo);
        return ResponseEntity.ok(transactionService.convertToDTO(transaction));
    }
}
