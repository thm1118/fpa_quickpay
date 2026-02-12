package com.fintech.quickpay.controller;

import com.fintech.quickpay.dto.AccountDTO;
import com.fintech.quickpay.entity.Account;
import com.fintech.quickpay.security.CurrentUser;
import com.fintech.quickpay.security.UserPrincipal;
import com.fintech.quickpay.service.AccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/account")
@RequiredArgsConstructor
public class AccountController {
    private final AccountService accountService;

    @GetMapping
    public ResponseEntity<AccountDTO> getMyAccount(@CurrentUser UserPrincipal principal) {
        Account account = accountService.getAccountByUser(principal.getUser());
        return ResponseEntity.ok(accountService.convertToDTO(account));
    }

    @GetMapping("/{accountNo}")
    public ResponseEntity<AccountDTO> getAccountInfo(@PathVariable String accountNo) {
        Account account = accountService.getAccountByAccountNo(accountNo);
        AccountDTO dto = new AccountDTO();
        dto.setAccountNo(account.getAccountNo());
        dto.setStatus(account.getStatus());
        // Only return limited info for other accounts
        return ResponseEntity.ok(dto);
    }
}
