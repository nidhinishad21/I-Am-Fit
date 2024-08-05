package com.example.iamfit.models;

public class ActivityItem {
    private String name;
    private String type;
    private String date;
    private int calories;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public int getCalories() {
        return calories;
    }

    public void setCalories(int calories) {
        this.calories = calories;
    }

    public void setActivityName(String activityName) {
        this.name = activityName;
    }

    public String getActivityName() {
        return name;
    };
}