package com.mealtime.tom.mealtime;

import java.util.Date;

public class MealCacheInfo {
    public int id;
    public long leftBeginSign;//左侧时间开始记录标记
    public long rightBeginSign;//右侧时间开始记录标记
    public int leftHistoryTime = 0;//左侧历史时间记录 单位秒
    public int rightHistoryTime = 0;//右侧历史时间记录 单位秒
    public int leftTime = 0;//左侧当前运行时间 单位秒
    public int rightTime = 0;//右侧当前运行时间 单位秒
    public boolean isLeft = false;//是否左侧正在记录时间
    public boolean isRight = false;//是否右侧正在记录时间
    public Date recordTime;//记录开始时间

    public void Clear()
    {
        leftBeginSign = 0;
        rightBeginSign = 0;
        leftHistoryTime = 0;
        rightHistoryTime = 0;
        leftTime = 0;
        rightTime = 0;
        isLeft = false;
        isRight = false;
        recordTime = null;
    }
}
