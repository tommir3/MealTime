package com.mealtime.tom.mealtime;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class DateHelper {
    private static final String FormatPattern = "yyyy-MM-dd HH:mm";

    /*获取当前之前几个月的日期*/
    public static Date GetBeforeMonthDate(Date signDate, int month){
        Date result;
        try{
            Calendar cal = Calendar.getInstance();
            cal.setTime(signDate);
            cal.add(Calendar.MONTH, -month);
            result = cal.getTime();
        }
        catch(Exception err){
            result = null;
        }
        return result;
    }

    public static Date GetThisYearDate(Date signDate){
        Date result;
        try{
            Calendar cal = Calendar.getInstance();
            cal.setTime(signDate);
            cal.set(cal.get(Calendar.YEAR),0,1,0,0);
            result = cal.getTime();
        }catch(Exception err){
            result = null;
        }
        return result;
    }

    public static Date StringToDate(String dateStr)
    {
        return StringToDate(dateStr, FormatPattern);
    }

    /*字符串转日期类*/
    public static Date StringToDate(String dateStr, String formatPattern)
    {
        Date result;
        try
        {
            SimpleDateFormat format = new SimpleDateFormat(formatPattern);
            result = format.parse(dateStr);
        }
        catch(Exception err)
        {
            result = null;
        }
        return result;
    }

    public static String DateToString(Date date)
    {
        return DateToString(date, FormatPattern);
    }

    public static String DateToString(Date date, String formatPattern)
    {
        String result;
        try
        {
            SimpleDateFormat format = new SimpleDateFormat(formatPattern);
            result = format.format(date);
        }
        catch(Exception err)
        {
            result = null;
        }
        return result;
    }

    public static int GetDaysBetweenDate(Date minDate, Date maxDate)
    {
        int result = 0;
        try
        {
            long minNum = minDate.getTime();
            long maxNum = maxDate.getTime();
            double val = (maxNum - minNum) / (60 * 1000 * 60 * 24);
            result = (int)Math.ceil(val);
        }
        catch(Exception err)
        {
            result = -1;
        }
        return result;
    }

    public static boolean CompareSameDay()
    {
        return false;
    }

    public static boolean IsSameDate(String cmp1, String cmp2, int type)
    {
        Date date1 = StringToDate(cmp1);
        Date date2 = StringToDate(cmp2);
        return IsSameDate(date1,date2,type);
    }

    public static boolean IsSameDate(Date cmp1, Date cmp2, int type)
    {
        boolean result = false;
        Calendar cal1 = Calendar.getInstance();
        cal1.setTime(cmp1);
        Calendar cal2 = Calendar.getInstance();
        cal2.setTime(cmp2);
        result = cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR);
        if(type == Calendar.YEAR)
        {
            return result;
        }
        result = cal1.get(Calendar.MONTH) == cal2.get(Calendar.MONTH);
        if(type == Calendar.MONTH)
        {
            return result;
        }
        result = cal1.get(Calendar.DAY_OF_MONTH) == cal2.get(Calendar.DAY_OF_MONTH);
        if(type == Calendar.DAY_OF_MONTH)
        {
            return result;
        }
        result = cal1.get(Calendar.HOUR) == cal2.get(Calendar.HOUR);
        if(type == Calendar.HOUR)
        {
            return result;
        }
        result = cal1.get(Calendar.MINUTE) == cal2.get(Calendar.MINUTE);
        if(type == Calendar.MINUTE)
        {
            return result;
        }
        result = cal1.get(Calendar.SECOND) == cal2.get(Calendar.SECOND);
        if(type == Calendar.SECOND)
        {
            return result;
        }
        return result;
    }

}
