package com.aetherlia.payment;

import com.fasterxml.jackson.annotation.JsonProperty;

public class SepayWebhookPayload {

    private Long id;

    private String gateway;

    @JsonProperty("transactionDate")
    private String transactionDate;

    private String accountNumber;

    private String subAccount;

    private String code;

    private String content;

    private String transferType;

    private String description;

    private Long transferAmount;

    private Long accumulated;

    private String referenceCode;

    public SepayWebhookPayload() {
    }

    public Long getId() {
        return id;
    }

    public String getGateway() {
        return gateway;
    }

    public String getTransactionDate() {
        return transactionDate;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public String getSubAccount() {
        return subAccount;
    }

    public String getCode() {
        return code;
    }

    public String getContent() {
        return content;
    }

    public String getTransferType() {
        return transferType;
    }

    public String getDescription() {
        return description;
    }

    public Long getTransferAmount() {
        return transferAmount;
    }

    public Long getAccumulated() {
        return accumulated;
    }

    public String getReferenceCode() {
        return referenceCode;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setGateway(String gateway) {
        this.gateway = gateway;
    }

    public void setTransactionDate(String transactionDate) {
        this.transactionDate = transactionDate;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public void setSubAccount(String subAccount) {
        this.subAccount = subAccount;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public void setTransferType(String transferType) {
        this.transferType = transferType;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setTransferAmount(Long transferAmount) {
        this.transferAmount = transferAmount;
    }

    public void setAccumulated(Long accumulated) {
        this.accumulated = accumulated;
    }

    public void setReferenceCode(String referenceCode) {
        this.referenceCode = referenceCode;
    }
}