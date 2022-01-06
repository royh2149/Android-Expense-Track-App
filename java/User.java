package com.example.expensetracker;

import org.bson.Document;

import java.util.ArrayList;

public class User {
    private String username;
    private String password;
    private double balance;

    // default constructor
    public User(String username, String password) {
        this.username = username;
        this.password = password;
        this.balance = 0.0;
    }

    // "full" constructor
    public User(String username, String password, double balance) {
        this.username = username;
        this.password = password;
        this.balance = balance;
    }

    // getters and setters for every attribute
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public double getBalance() {
        return balance;
    }

    public void setBalance(double balance) {
        this.balance = balance;
    }
}
