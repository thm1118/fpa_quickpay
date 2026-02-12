package com.fintech.quickpay.dto;

import com.fintech.quickpay.entity.BankCard;
import lombok.Data;

@Data
public class BankCardDTO {
    private Long id;
    private String maskedCardNo;
    private String bankName;
    private String cardHolder;
    private BankCard.CardType cardType;
    private Boolean isDefault;
}
