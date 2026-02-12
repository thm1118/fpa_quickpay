package com.fintech.quickpay.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "bank_cards")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BankCard {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 20)
    private String cardNo;

    @Column(nullable = false, length = 50)
    private String bankName;

    @Column(length = 50)
    private String cardHolder;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private CardType cardType = CardType.DEBIT;

    @Column(nullable = false)
    private Boolean isDefault = false;

    @Column(nullable = false)
    private Boolean active = true;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public enum CardType {
        DEBIT,
        CREDIT
    }

    public String getMaskedCardNo() {
        if (cardNo != null && cardNo.length() > 8) {
            return cardNo.substring(0, 4) + "****" + cardNo.substring(cardNo.length() - 4);
        }
        return cardNo;
    }
}
