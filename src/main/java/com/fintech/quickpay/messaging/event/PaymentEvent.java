package com.fintech.quickpay.messaging.event;

import java.math.BigDecimal;

public class PaymentEvent {
    private String type;
    private String transactionNo;
    private String username;
    private String fromAccountNo;
    private String toAccountNo;
    private BigDecimal amount;

    public PaymentEvent() {}

    public PaymentEvent(String type, String transactionNo, String username,
                        String fromAccountNo, String toAccountNo, BigDecimal amount) {
        this.type = type;
        this.transactionNo = transactionNo;
        this.username = username;
        this.fromAccountNo = fromAccountNo;
        this.toAccountNo = toAccountNo;
        this.amount = amount;
    }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getTransactionNo() { return transactionNo; }
    public void setTransactionNo(String transactionNo) { this.transactionNo = transactionNo; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getFromAccountNo() { return fromAccountNo; }
    public void setFromAccountNo(String fromAccountNo) { this.fromAccountNo = fromAccountNo; }

    public String getToAccountNo() { return toAccountNo; }
    public void setToAccountNo(String toAccountNo) { this.toAccountNo = toAccountNo; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
}
