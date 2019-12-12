package com.mealtime.tom.mealtime;

import java.io.Serializable;
import java.util.Date;

public class MealInfo implements Serializable, Comparable<MealInfo> {
    public int flowID;
    public String dateStr;
    public int mealType;
    public int leftTime;
    public int rightTime;
    public int totalTime;
    public String remark;
    //秒 转 分，向上取整，不足一分钟按一分钟处理
    public static int SecondToMinute(double val)
    {
        int result;
        try
        {
            result = (int)(val / 60);
            if(val % 60 > 0)
            {
                result += 1;
            }
        }
        catch(Exception err)
        {
            result = 0;
            System.out.printf("\nSecondToMinute catch: " + err.getMessage() + "\n");
        }
        return result;
    }

    public static int MinuteToSecond(int val)
    {
        return val * 60;
    }

    @Override
    public int compareTo(MealInfo o) {
        int result;
        try
        {
            Date curDate = DateHelper.StringToDate(this.dateStr);
            Date oDaTE = DateHelper.StringToDate(o.dateStr);
            result = curDate.compareTo(oDaTE);
        }
        catch(Exception err)
        {
            result = 0;
            System.out.printf("\n" + err.getMessage() + "\n");
        }
        return result;
    }
}
