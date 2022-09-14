package com.gomspace.fundAPI.entity;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.lang.NonNull;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.math.BigDecimal;

@Entity
public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @NonNull
    private Long owner;
    @NonNull
    private String currency;
    @NonNull
    private BigDecimal balance;

    public Account() {
    }

    public Account(Long owner, String currency, BigDecimal balance) {
        this.owner = owner;
        this.currency = currency;
        this.balance = balance;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    public Long getOwner() {
        return owner;
    }

    public void setOwner(Long ownerID) {
        this.owner = ownerID;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }

    @Override
    public String toString() {
        return "Account{" +
                "id=" + id +
                ", owner=" + owner +
                ", currency='" + currency + '\'' +
                ", balance=" + balance +
                '}';
    }
}