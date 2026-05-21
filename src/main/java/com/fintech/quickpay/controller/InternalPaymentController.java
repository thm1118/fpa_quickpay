package com.fintech.quickpay.controller;

import com.fintech.quickpay.dto.AccountDTO;
import com.fintech.quickpay.dto.BankCardDTO;
import com.fintech.quickpay.dto.TransactionDTO;
import com.fintech.quickpay.entity.Account;
import com.fintech.quickpay.entity.User;
import com.fintech.quickpay.repository.UserRepository;
import com.fintech.quickpay.service.AccountService;
import com.fintech.quickpay.service.BankCardService;
import com.fintech.quickpay.service.TransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 内部服务间调用接口，通过 X-Service-Key 校验，不走JWT认证
 */
@RestController
@RequestMapping("/internal/payment")
@RequiredArgsConstructor
public class InternalPaymentController {

    private final AccountService accountService;
    private final TransactionService transactionService;
    private final BankCardService bankCardService;
    private final UserRepository userRepository;

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

    /**
     * 内部查询账户接口 —— 供SmartWallet调用
     * 根据用户名返回该用户的支付账户信息
     */
    @GetMapping("/account")
    public ResponseEntity<?> internalGetAccount(
            @RequestHeader("X-Service-Key") String serviceKey,
            @RequestParam("username") String username) {

        validateServiceKey(serviceKey);

        User user = userRepository.findByUsername(username).orElse(null);
        if (user == null) {
            return ResponseEntity.notFound().build();
        }

        Account account = accountService.getAccountByUser(user);
        AccountDTO dto = accountService.convertToDTO(account);
        return ResponseEntity.ok(dto);
    }

    /**
     * 内部查询交易记录接口 —— 供SmartWallet调用
     * 根据用户名返回该用户的交易记录（最近20条）
     */
    @GetMapping("/transactions")
    public ResponseEntity<?> internalGetTransactions(
            @RequestHeader("X-Service-Key") String serviceKey,
            @RequestParam("username") String username) {

        validateServiceKey(serviceKey);

        User user = userRepository.findByUsername(username).orElse(null);
        if (user == null) {
            return ResponseEntity.notFound().build();
        }

        Page<TransactionDTO> page = transactionService.getUserTransactions(
                user, PageRequest.of(0, 20));
        return ResponseEntity.ok(page.getContent());
    }

    /**
     * 内部查询绑定银行卡接口 —— 供SmartWallet调用
     * 根据用户名返回该用户绑定的银行卡列表
     */
    @GetMapping("/cards")
    public ResponseEntity<?> internalGetCards(
            @RequestHeader("X-Service-Key") String serviceKey,
            @RequestParam("username") String username) {

        validateServiceKey(serviceKey);

        User user = userRepository.findByUsername(username).orElse(null);
        if (user == null) {
            return ResponseEntity.notFound().build();
        }

        List<BankCardDTO> cards = bankCardService.getUserCards(user);
        return ResponseEntity.ok(cards);
    }

    private void validateServiceKey(String serviceKey) {
        if (!internalKey.equals(serviceKey)) {
            throw new IllegalArgumentException("Invalid service key");
        }
    }
}
