package com.example.expensetracker;

import android.content.Context;
import android.graphics.Color;

import java.time.LocalDateTime;

public class Outcome extends Action{

    public Outcome(double sum, String category, String desc, String image, String username, LocalDateTime date) {
        super(sum, category, desc, image, username, date);
    }

    @Override
    public int getColor(Context context) {
        return context.getResources().getColor(R.color.outcomeColor, null);
    }
}
