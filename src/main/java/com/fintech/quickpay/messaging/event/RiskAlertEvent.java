package com.fintech.quickpay.messaging.event;

public class RiskAlertEvent {
    private String type;
    private String customerNo;
    private String riskLevel;
    private Integer score;
    private String description;

    public RiskAlertEvent() {}

    public RiskAlertEvent(String type, String customerNo, String riskLevel, Integer score, String description) {
        this.type = type;
        this.customerNo = customerNo;
        this.riskLevel = riskLevel;
        this.score = score;
        this.description = description;
    }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getCustomerNo() { return customerNo; }
    public void setCustomerNo(String customerNo) { this.customerNo = customerNo; }

    public String getRiskLevel() { return riskLevel; }
    public void setRiskLevel(String riskLevel) { this.riskLevel = riskLevel; }

    public Integer getScore() { return score; }
    public void setScore(Integer score) { this.score = score; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}
