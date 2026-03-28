package com.fintech.quickpay.controller;

import com.fintech.quickpay.client.CoreBankingClient;
import com.fintech.quickpay.client.RiskControlClient;
import com.fintech.quickpay.dto.RechargeRequest;
import com.fintech.quickpay.dto.TransactionDTO;
import com.fintech.quickpay.dto.TransferRequest;
import com.fintech.quickpay.security.CurrentUser;
import com.fintech.quickpay.security.UserPrincipal;
import com.fintech.quickpay.service.TransactionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;

@RestController
@RequestMapping("/api/payment")
@RequiredArgsConstructor
public class PaymentController {
    private final TransactionService transactionService;
    private final CoreBankingClient coreBankingClient;
    private final RiskControlClient riskControlClient;

    @PostMapping("/transfer")
    public ResponseEntity<TransactionDTO> transfer(
            @CurrentUser UserPrincipal principal,
            @Valid @RequestBody TransferRequest request) {
        TransactionDTO transaction = transactionService.transfer(principal.getUser(), request);
        return ResponseEntity.ok(transaction);
    }

    @PostMapping("/recharge")
    public ResponseEntity<TransactionDTO> recharge(
            @CurrentUser UserPrincipal principal,
            @Valid @RequestBody RechargeRequest request) {
        TransactionDTO transaction = transactionService.recharge(principal.getUser(), request);
        return ResponseEntity.ok(transaction);
    }

    @PostMapping("/withdraw")
    public ResponseEntity<TransactionDTO> withdraw(
            @CurrentUser UserPrincipal principal,
            @RequestBody Map<String, Object> request) {
        BigDecimal amount = new BigDecimal(request.get("amount").toString());
        String paymentPassword = (String) request.get("paymentPassword");
        TransactionDTO transaction = transactionService.withdraw(principal.getUser(), amount, paymentPassword);
        return ResponseEntity.ok(transaction);
    }

    @PostMapping("/settle")
    public ResponseEntity<Map<String, Object>> settle(
            @CurrentUser UserPrincipal principal,
            @RequestBody Map<String, Object> request) {
        String accountNo = (String) request.get("accountNo");
        BigDecimal amount = new BigDecimal(request.get("amount").toString());
        String remark = (String) request.get("remark");
        Map<String, Object> result = coreBankingClient.settle(accountNo, amount, remark);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/risk-check")
    public ResponseEntity<Map<String, Object>> riskCheck(
            @CurrentUser UserPrincipal principal,
            @RequestBody Map<String, Object> request) {
        String customerNo = (String) request.get("customerNo");
        String transactionType = (String) request.get("transactionType");
        BigDecimal amount = new BigDecimal(request.get("amount").toString());
        String accountNo = (String) request.get("accountNo");
        Map<String, Object> result = riskControlClient.checkTransaction(customerNo, transactionType, amount, accountNo);
        return ResponseEntity.ok(result);
    }
}
