package com.fintech.quickpay.controller;

import com.fintech.quickpay.entity.Account;
import com.fintech.quickpay.service.AccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;

/**
 * 内部服务间调用接口，通过 X-Service-Key 校验，不走JWT认证
 */
@RestController
@RequestMapping("/internal/payment")
@RequiredArgsConstructor
public class InternalPaymentController {

    private final AccountService accountService;

    @Value("${service.internal-key}")
    private String internalKey;

    /**
     * 内部转账接口 —— 供SmartWallet调用
     * 从指定账户扣款并转入目标账户
     */
    @PostMapping("/transfer")
    public ResponseEntity<?> internalTransfer(
            @RequestHeader("X-Service-Key") String serviceKey,
            @RequestBody Map<String, Object> request) {

        validateServiceKey(serviceKey);

        String fromAccountNo = (String) request.get("fromAccountNo");
        String toAccountNo = (String) request.get("toAccountNo");
        BigDecimal amount = new BigDecimal(request.get("amount").toString());

        Account fromAccount = accountService.getAccountByAccountNo(fromAccountNo);
        Account toAccount = accountService.getAccountByAccountNo(toAccountNo);

        // 检查余额
        BigDecimal available = fromAccount.getBalance().subtract(fromAccount.getFrozenAmount());
        if (available.compareTo(amount) < 0) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Insufficient balance"
            ));
        }

        // 执行转账
        accountService.subtractBalance(fromAccount, amount);
        accountService.addBalance(toAccount, amount);

        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Transfer completed",
                "fromAccountNo", fromAccountNo,
                "toAccountNo", toAccountNo,
                "amount", amount
        ));
    }

    /**
     * 内部充值接口 —— 供TradeSim调用
     * 向指定账户充值（模拟券商资金划转到支付账户）
     */
    @PostMapping("/recharge")
    public ResponseEntity<?> internalRecharge(
            @RequestHeader("X-Service-Key") String serviceKey,
            @RequestBody Map<String, Object> request) {

        validateServiceKey(serviceKey);

        String accountNo = (String) request.get("accountNo");
        BigDecimal amount = new BigDecimal(request.get("amount").toString());

        Account account = accountService.getAccountByAccountNo(accountNo);
        accountService.addBalance(account, amount);

        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Recharge completed",
                "accountNo", accountNo,
                "amount", amount
        ));
    }

    private void validateServiceKey(String serviceKey) {
        if (!internalKey.equals(serviceKey)) {
            throw new IllegalArgumentException("Invalid service key");
        }
    }
}
