package com.gomspace.fundAPI.entity;

import java.math.BigDecimal;

public class Transfer {

    private long source;
    private long target;
    private BigDecimal amount;

    public Transfer() {
    }

    public Transfer(long source, long target, BigDecimal amount) {
        this.source = source;
        this.target = target;
        this.amount = amount;
    }

    public long getSource() {
        return source;
    }

    public void setSource(long source) {
        this.source = source;
    }

    public long getTarget() {
        return target;
    }

    public void setTarget(long target) {
        this.target = target;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    @Override
    public String toString() {
        return "Transfer{" +
                "source=" + source +
                ", target=" + target +
                ", amount=" + amount +
                '}';
    }
}
