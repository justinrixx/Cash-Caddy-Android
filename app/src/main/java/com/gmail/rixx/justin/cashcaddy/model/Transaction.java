package com.gmail.rixx.justin.cashcaddy.model;


/**
 * Transaction POJO
 */
public class Transaction {

    private int amount;
    private String category;
    private String comment;
    private String date;

    public Transaction(int amount, String category, String comment, String date) {
        this.amount = amount;
        this.category = category;
        this.comment = comment;
        this.date = date;
    }

    public Transaction() {
    }

    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }
}
