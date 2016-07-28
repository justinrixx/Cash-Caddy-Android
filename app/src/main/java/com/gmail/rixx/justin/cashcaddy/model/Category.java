package com.gmail.rixx.justin.cashcaddy.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.firebase.database.Exclude;

import java.util.HashMap;
import java.util.Map;

/**
 * Just a POJO to use FirebaseUI
 * https://github.com/firebase/FirebaseUI-Android/issues/72
 * https://firebase.google.com/support/guides/firebase-android (@JsonIgnore)
 */
public class Category implements Parcelable {

    private int amount;
    private int balance;
    private String lastRefresh;
    private String name;
    private String refreshCode;

    @Exclude
    private String key;

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

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("amount", amount);
        result.put("balance", balance);
        result.put("lastRefresh", lastRefresh);
        result.put("name", name);
        result.put("refreshCode", refreshCode);

        return result;
    }

    protected Category(Parcel in) {
        amount = in.readInt();
        balance = in.readInt();
        lastRefresh = in.readString();
        name = in.readString();
        refreshCode = in.readString();
        key = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(amount);
        dest.writeInt(balance);
        dest.writeString(lastRefresh);
        dest.writeString(name);
        dest.writeString(refreshCode);
        dest.writeString(key);
    }

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<Category> CREATOR = new Parcelable.Creator<Category>() {
        @Override
        public Category createFromParcel(Parcel in) {
            return new Category(in);
        }

        @Override
        public Category[] newArray(int size) {
            return new Category[size];
        }
    };
}