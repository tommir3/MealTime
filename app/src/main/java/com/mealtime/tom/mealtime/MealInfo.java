package com.mealtime.tom.mealtime;

import java.io.Serializable;

public class MealInfo implements Serializable {
    public int flowID;
    public String dateStr;
    public int mealType;
    public int leftTime;
    public int rightTime;
    public int totalTime;
    public String remark;
}
