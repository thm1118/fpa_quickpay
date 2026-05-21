package com.fintech.quickpay.messaging.event;

import java.math.BigDecimal;

public class NotificationEvent {
    private String transactionNo;
    private String type;
    private Long fromUserId;
    private Long toUserId;
    private BigDecimal amount;

    public NotificationEvent() {}

    public NotificationEvent(String transactionNo, String type, Long fromUserId, Long toUserId, BigDecimal amount) {
        this.transactionNo = transactionNo;
        this.type = type;
        this.fromUserId = fromUserId;
        this.toUserId = toUserId;
        this.amount = amount;
    }

    public String getTransactionNo() { return transactionNo; }
    public void setTransactionNo(String transactionNo) { this.transactionNo = transactionNo; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public Long getFromUserId() { return fromUserId; }
    public void setFromUserId(Long fromUserId) { this.fromUserId = fromUserId; }

    public Long getToUserId() { return toUserId; }
    public void setToUserId(Long toUserId) { this.toUserId = toUserId; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
}
