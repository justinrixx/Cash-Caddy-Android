package com.gmail.rixx.justin.cashcaddy.model;

/**
 * Just a POJO to use FirebaseUI
 */
public class Category {

    private int amount;
    private int balance;
    private String lastRefresh;
    private String name;
    private String refreshCode;

    public Category(int amount, int balance, String lastRefresh, String name, String refreshCode) {
        this.amount = amount;
        this.balance = balance;
        this.lastRefresh = lastRefresh;
        this.name = name;
        this.refreshCode = refreshCode;
    }

    public Category() {
    }

    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    public int getBalance() {
        return balance;
    }

    public void setBalance(int balance) {
        this.balance = balance;
    }

    public String getLastRefresh() {
        return lastRefresh;
    }

    public void setLastRefresh(String lastRefresh) {
        this.lastRefresh = lastRefresh;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRefreshCode() {
        return refreshCode;
    }

    public void setRefreshCode(String refreshCode) {
        this.refreshCode = refreshCode;
    }
}
