package com.example.iamfit.models;

import java.util.Date;

public class HistoryItem {
    Date date;
    int foodCalories;
    int exerciseCalories;
    int netCalories;

    public HistoryItem(Date date, int foodCalories, int exerciseCalories, int netCalories) {
        this.date = date;
        this.foodCalories = foodCalories;
        this.exerciseCalories = exerciseCalories;
        this.netCalories = netCalories;
    }
}