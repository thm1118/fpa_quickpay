package com.fintech.quickpay.controller;

import com.fintech.quickpay.dto.BankCardDTO;
import com.fintech.quickpay.entity.BankCard;
import com.fintech.quickpay.security.CurrentUser;
import com.fintech.quickpay.security.UserPrincipal;
import com.fintech.quickpay.service.BankCardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/bank-cards")
@RequiredArgsConstructor
public class BankCardController {
    private final BankCardService bankCardService;

    @GetMapping
    public ResponseEntity<List<BankCardDTO>> getMyCards(@CurrentUser UserPrincipal principal) {
        List<BankCardDTO> cards = bankCardService.getUserCards(principal.getUser());
        return ResponseEntity.ok(cards);
    }

    @PostMapping
    public ResponseEntity<BankCardDTO> bindCard(
            @CurrentUser UserPrincipal principal,
            @RequestBody Map<String, String> request) {
        String cardNo = request.get("cardNo");
        String bankName = request.get("bankName");
        String cardHolder = request.get("cardHolder");
        BankCard.CardType cardType = BankCard.CardType.valueOf(request.getOrDefault("cardType", "DEBIT"));

        BankCardDTO card = bankCardService.bindCard(principal.getUser(), cardNo, bankName, cardHolder, cardType);
        return ResponseEntity.ok(card);
    }

    @DeleteMapping("/{cardId}")
    public ResponseEntity<Void> unbindCard(
            @CurrentUser UserPrincipal principal,
            @PathVariable Long cardId) {
        bankCardService.unbindCard(principal.getUser(), cardId);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{cardId}/default")
    public ResponseEntity<Void> setDefaultCard(
            @CurrentUser UserPrincipal principal,
            @PathVariable Long cardId) {
        bankCardService.setDefaultCard(principal.getUser(), cardId);
        return ResponseEntity.ok().build();
    }
}
