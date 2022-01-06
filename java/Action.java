package com.example.expensetracker;

import android.content.Context;

import org.bson.BsonObjectId;
import org.bson.Document;
import org.bson.types.ObjectId;

import java.time.LocalDateTime;

public abstract class Action {

    private double sum;
    private String category;
    private String desc;
    private String image;
    private String username;
    private LocalDateTime date;
    private ObjectId actionId;

    public Action(double sum, String category, String desc, String image, String username, LocalDateTime date) {
        this.sum = sum;
        this.category = category;
        this.desc = desc;
        this.image = image;
        this.username = username;
        this.date = date;
    }

    public abstract int getColor(Context context);

    public double getSum() {
        return sum;
    }

    public void setSum(double sum) {
        this.sum = sum;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public LocalDateTime getDate() {
        return date;
    }

    public void setDate(LocalDateTime date) {
        this.date = date;
    }

    public ObjectId getActionId() {
        return actionId;
    }

    public void setActionId(ObjectId actionId) {
        this.actionId = actionId;
    }

    @Override
    public String toString() {
        return "Action{" +
                "sum=" + sum +
                ", category='" + category + '\'' +
                ", desc='" + desc + '\'' +
                ", username='" + username + '\'' +
                ", date=" + date.toString() +
                ", actionId=" + actionId.toString() +
                '}';
    }
}
