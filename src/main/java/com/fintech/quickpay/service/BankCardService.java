package com.fintech.quickpay.service;

import com.fintech.quickpay.dto.BankCardDTO;
import com.fintech.quickpay.entity.BankCard;
import com.fintech.quickpay.entity.User;
import com.fintech.quickpay.exception.BusinessException;
import com.fintech.quickpay.exception.ResourceNotFoundException;
import com.fintech.quickpay.repository.BankCardRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BankCardService {
    private final BankCardRepository bankCardRepository;

    @Transactional
    public BankCardDTO bindCard(User user, String cardNo, String bankName, String cardHolder, BankCard.CardType cardType) {
        // Check if card already bound
        if (bankCardRepository.existsByCardNo(cardNo)) {
            throw new BusinessException("This card is already bound to an account");
        }

        BankCard card = new BankCard();
        card.setUser(user);
        card.setCardNo(cardNo);
        card.setBankName(bankName);
        card.setCardHolder(cardHolder);
        card.setCardType(cardType);

        // If this is the first card, set as default
        if (bankCardRepository.countByUser(user) == 0) {
            card.setIsDefault(true);
        }

        card = bankCardRepository.save(card);
        return convertToDTO(card);
    }

    @Transactional
    public void unbindCard(User user, Long cardId) {
        BankCard card = bankCardRepository.findById(cardId)
                .orElseThrow(() -> new ResourceNotFoundException("Bank card not found"));

        if (!card.getUser().getId().equals(user.getId())) {
            throw new BusinessException("You don't have permission to unbind this card");
        }

        boolean wasDefault = card.getIsDefault();
        bankCardRepository.delete(card);

        // If deleted card was default, set another as default
        if (wasDefault) {
            List<BankCard> remaining = bankCardRepository.findByUser(user);
            if (!remaining.isEmpty()) {
                remaining.get(0).setIsDefault(true);
                bankCardRepository.save(remaining.get(0));
            }
        }
    }

    @Transactional
    public void setDefaultCard(User user, Long cardId) {
        BankCard card = bankCardRepository.findById(cardId)
                .orElseThrow(() -> new ResourceNotFoundException("Bank card not found"));

        if (!card.getUser().getId().equals(user.getId())) {
            throw new BusinessException("You don't have permission to modify this card");
        }

        // Remove default from all user's cards
        List<BankCard> userCards = bankCardRepository.findByUser(user);
        userCards.forEach(c -> c.setIsDefault(false));
        bankCardRepository.saveAll(userCards);

        // Set new default
        card.setIsDefault(true);
        bankCardRepository.save(card);
    }

    public List<BankCardDTO> getUserCards(User user) {
        return bankCardRepository.findByUser(user).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public BankCard getDefaultCard(User user) {
        return bankCardRepository.findByUserAndIsDefaultTrue(user)
                .orElseThrow(() -> new ResourceNotFoundException("No default bank card found"));
    }

    public BankCardDTO convertToDTO(BankCard card) {
        BankCardDTO dto = new BankCardDTO();
        dto.setId(card.getId());
        dto.setMaskedCardNo(maskCardNo(card.getCardNo()));
        dto.setBankName(card.getBankName());
        dto.setCardHolder(card.getCardHolder());
        dto.setCardType(card.getCardType());
        dto.setIsDefault(card.getIsDefault());
        return dto;
    }

    private String maskCardNo(String cardNo) {
        if (cardNo == null || cardNo.length() < 8) {
            return "****";
        }
        return cardNo.substring(0, 4) + "****" + cardNo.substring(cardNo.length() - 4);
    }
}
