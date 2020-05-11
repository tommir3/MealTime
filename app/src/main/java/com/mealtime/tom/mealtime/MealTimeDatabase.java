package com.mealtime.tom.mealtime;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MealTimeDatabase extends SQLiteOpenHelper {

    private final static String _mealTableName = "MealInfo";
    private final static String _mealCacheTableName = "MealCache";
    private final static int _mealDbVersion = 9;

    public MealTimeDatabase(Context context)
    {
        super(context,_mealTableName,null,_mealDbVersion);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        CreateMealTable(db);
        CreateMealTimeCacheTable(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
    {
        if(!IsExistTable(db, _mealTableName))
        {
            CreateMealTable(db);
        }
        if(!IsExistTable(db, _mealCacheTableName))
        {
            CreateMealTimeCacheTable(db);
        }
        MealInfo[] infos = GetMealInfos(db, 0);
        if(infos != null)
        {
            for(int i = 0; i < infos.length; ++i)
            {
                String convertStr = DateHelper.DateToString(DateHelper.StringToDate(infos[i].dateStr));
                if(convertStr != infos[i].dateStr)
                {
                    infos[i].dateStr = convertStr;
                    UpdateMealInfo(db, infos[i]);

                }
            }
        }
    }

    public int AddMealTimeCache(MealCacheInfo cacheInfo)
    {
        int result;
        try
        {
            ContentValues values = new ContentValues();
            values.put("leftBeginSign",cacheInfo.leftBeginSign);
            values.put("rightBeginSign",cacheInfo.rightBeginSign);
            values.put("leftHistoryTime",cacheInfo.leftHistoryTime);
            values.put("rightHistoryTime",cacheInfo.rightHistoryTime);
            values.put("leftTime",cacheInfo.leftTime);
            values.put("rightTime",cacheInfo.rightTime);
            values.put("isLeft",cacheInfo.isLeft);
            values.put("isRight",cacheInfo.isRight);
            String recordTimeStr = DateHelper.DateToString(cacheInfo.recordTime);
            values.put("recordTime",recordTimeStr);
            SQLiteDatabase db = getWritableDatabase();
            long newID = db.insert(_mealCacheTableName, null, values);
            result = (int)newID;
        }
        catch(Exception err)
        {
            result = -1;
            System.out.printf(err.getMessage());
        }
        return result;
    }
    public MealCacheInfo GetMealTimeCache()
    {
        MealCacheInfo result = null;
        try
        {
            SQLiteDatabase db = getReadableDatabase();
            Cursor bakData = db.query(_mealCacheTableName,null,null,null,"","","id desc","1");
            if(bakData != null)
            {
                result = CursorToMealCacheInfo(bakData);
            }
        }
        catch(Exception err)
        {
            result = null;
        }
        return result;
    }
    public boolean DeleteMealTimeCache()
    {
        boolean result;
        try
        {
            SQLiteDatabase db = getWritableDatabase();
            int bakVal = db.delete(_mealCacheTableName,null,null);
            result = (bakVal > 0) ? true : false;
        }
        catch(Exception err)
        {
            result = false;
            System.out.printf(err.getMessage());
        }
        return result;
    }
    public boolean IsExistTable(SQLiteDatabase db, String tableName)
    {
        boolean result = false;
        try
        {
            String sqlTest = "SELECT count(*) FROM sqlite_master WHERE type='table' AND name='" + tableName + "';";
            Cursor bakData = db.rawQuery(sqlTest,null);
            if(bakData != null)
            {
                bakData.moveToFirst();
                result = (bakData.getInt(0) == 0) ? false : true;
            }
        }
        catch(Exception err)
        {
            result = false;
            System.out.printf(err.getMessage());
        }
        return result;
    }
    private void CreateMealTimeCacheTable(SQLiteDatabase db)
    {
        try
        {
            /*
            create table AAA(
            id int primary key AUTOINCREMENT,
            leftBeginSign int not null default 0,
            rightBeginSign int not null default 0,
            leftHistoryTime int not null default 0,
            rightHistoryTime int not null default 0,
            leftTime int not null default 0,
            rightTime int not null default 0,
            isLeft int not null default 0,
            isRight int not null default 0,
            recordTime text null
            )
             */
            StringBuilder sql = new StringBuilder();
            sql.append("create table IF NOT EXISTS ");
            sql.append(_mealCacheTableName);
            sql.append("(");
            sql.append("id Integer primary key AUTOINCREMENT,");
            sql.append("leftBeginSign int not null default 0,");
            sql.append("rightBeginSign int not null default 0,");
            sql.append("leftHistoryTime int not null default 0,");
            sql.append("rightHistoryTime int not null default 0,");
            sql.append("leftTime int not null default 0,");
            sql.append("rightTime int not null default 0,");
            sql.append("isLeft int not null default 0,");
            sql.append("isRight int not null default 0,");
            sql.append("recordTime text null");
            sql.append(")");
            db.execSQL(sql.toString());
        }
        catch(Exception err)
        {
            System.out.printf(err.getMessage());
        }
    }
    //创建用餐信息表
    private void CreateMealTable(SQLiteDatabase db)
    {
        try
        {
            /*"
            create table AAA(
            id int primary key AUTOINCREMENT,
            date text not null,
            type int not null default 1,
            leftValue int not null default 0,
            rightValue int not null default 0,
            value int not null default 0,
            remark text null
            )"
             */
            StringBuilder sql = new StringBuilder();
            sql.append("create table IF NOT EXISTS ");
            sql.append(_mealTableName);
            sql.append("(");
            sql.append("id Integer primary key AUTOINCREMENT,");
            sql.append("date text not null,");
            sql.append("type int not null default 1,");
            sql.append("leftValue int not null default 0,");
            sql.append("rightValue int not null default 0,");
            sql.append("value int not null default 0,");
            sql.append("remark text null");
            sql.append(")");
            db.execSQL(sql.toString());
        }
        catch(Exception err)
        {
            System.out.printf(err.getMessage());
        }
    }
    //添加用餐信息
    public int AddMealInfo(MealInfo info)
    {
        int result;
        try
        {
            ContentValues values = new ContentValues();
            values.put("date",info.dateStr);
            values.put("type",info.mealType);
            values.put("leftValue",info.leftTime);
            values.put("rightValue",info.rightTime);
            values.put("value",info.totalTime);
            values.put("remark",info.remark);
            SQLiteDatabase db = getWritableDatabase();
            long newID = db.insert(_mealTableName, null, values);
            result = (int)newID;
        }
        catch(Exception err)
        {
            result = -1;
            System.out.printf(err.getMessage());
        }
        return result;
    }
    //获指定ID用餐信息
    public MealInfo GetMealInfo(int id)
    {
        MealInfo result = null;
        try
        {
            SQLiteDatabase db = getReadableDatabase();
            Cursor bakData = db.query(_mealTableName,null,"id=?",new String[]{ Integer.toString(id) },"","","","");
            if(bakData != null)
            {
                MealInfo[] toVals = CursorToMealInfos(bakData);
                if(toVals != null && toVals.length == 1)
                {
                    result = toVals[0];
                }
            }
        }
        catch(Exception err)
        {
            result = null;
        }
        return result;
    }
    //更新指定ID用餐信息
    public boolean UpdateMealInfo(MealInfo info)
    {
        return UpdateMealInfo(getReadableDatabase(), info);
    }
    //删除指定ID用餐信息
    public boolean DelMealInfo(int id)
    {
        boolean result;
        try
        {
            SQLiteDatabase db = getWritableDatabase();
            int bakVal = db.delete(_mealTableName,"id=?",new String[]{ Integer.toString(id) });
            result = (bakVal == 1) ? true : false;
        }
        catch(Exception err)
        {
            result = false;
            System.out.printf(err.getMessage());
        }
        return result;
    }
    //获取所有用餐信息
    //topCount: 小于等于0时，获取全部信息
    public MealInfo[] GetMealInfos(int topCount)
    {
        return GetMealInfos(getReadableDatabase(),topCount);
    }
    //获取最后一次用餐信息
    public MealInfo GetLastMealInfo()
    {
        MealInfo result = null;
        try
        {
            SQLiteDatabase db = getReadableDatabase();
            Cursor bakData = db.query(_mealTableName,null,"",null,"","","date desc","1");
            if(bakData != null)
            {
                MealInfo[] infos = CursorToMealInfos(bakData);
                if(infos != null && infos.length > 0)
                {
                    result = infos[0];
                }
            }
        }
        catch(Exception err)
        {
            result = null;
            System.out.printf(err.getMessage());
        }
        return result;
    }
    public int GetMealCountByDay(String dateStr)
    {
        int result = -1;
        try
        {
            SQLiteDatabase db = getReadableDatabase();
            String subDateStr = dateStr.substring(0,10);
            Cursor bakData = db.query(_mealTableName,new String[]{"count(id)"},"substr(date, 1, 10) = ?",new String[]{ subDateStr },"","","","");
            if(bakData != null)
            {
                bakData.moveToFirst();
                result = bakData.getInt(0);
            }
        }
        catch(Exception err)
        {
            result = -1;
            System.out.printf(err.getMessage());
        }
        return result;
    }
    //更新指定ID用餐信息
    private boolean UpdateMealInfo(SQLiteDatabase db, MealInfo info)
    {
        boolean result;
        try
        {
            ContentValues values = new ContentValues();
            values.put("date",info.dateStr);
            values.put("type",info.mealType);
            values.put("leftValue",info.leftTime);
            values.put("rightValue",info.rightTime);
            values.put("value",info.totalTime);
            values.put("remark",info.remark);
            int bakVal = db.update(_mealTableName,values,"id=?",new String[]{ Integer.toString(info.flowID) });
            result = (bakVal == 1) ? true : false;
        }
        catch(Exception err)
        {
            result = false;
            System.out.printf(err.getMessage());
        }
        return result;
    }
    //获取所有用餐信息
    private MealInfo[] GetMealInfos(SQLiteDatabase db, int topCount)
    {
        MealInfo[] result = null;
        try
        {
            String limitStr = (topCount <= 0) ? "" : String.valueOf(topCount);
            String orderByStr = (topCount <= 0) ? "datetime(date)" : "datetime(date) desc";
            Cursor bakData = db.query(_mealTableName,null,null,null,"","",orderByStr,limitStr);
            if(bakData != null)
            {
                result = CursorToMealInfos(bakData);
            }
        }
        catch(Exception err)
        {
            result = null;
            System.out.printf(err.getMessage());
        }
        return result;
    }
    //从游标中读取用餐信息
    private MealInfo[] CursorToMealInfos(Cursor ptr)
    {
        MealInfo[] result = null;
        try
        {
            if(ptr != null)
            {
                List<MealInfo> list = new ArrayList<MealInfo>();
                ptr.moveToFirst();
                while(!ptr.isAfterLast())
                {
                    MealInfo info = new MealInfo();
                    info.flowID = ptr.getInt(ptr.getColumnIndex("id"));
                    info.dateStr = ptr.getString(ptr.getColumnIndex("date"));
                    info.mealType = ptr.getInt(ptr.getColumnIndex("type"));
                    info.leftTime = ptr.getInt(ptr.getColumnIndex("leftValue"));
                    info.rightTime = ptr.getInt(ptr.getColumnIndex("rightValue"));
                    info.totalTime = ptr.getInt(ptr.getColumnIndex("value"));
                    info.remark = ptr.getString(ptr.getColumnIndex("remark"));
                    list.add(info);
                    ptr.moveToNext();
                }
                if(list.size() > 0)
                {
                    Collections.sort(list);
                    result = new MealInfo[list.size()];
                    for(int i = 0; i < list.size(); ++i)
                    {
                        result[i] = list.get(i);
                    }
                }
            }
        }
        catch(Exception err)
        {
            result = null;
            System.out.printf(err.getMessage());
        }
        return result;
    }

    private MealCacheInfo CursorToMealCacheInfo(Cursor ptr)
    {
        MealCacheInfo result = null;
        try
        {
            if(ptr != null)
            {
                ptr.moveToFirst();
                result = new MealCacheInfo();
                result.id = ptr.getInt(ptr.getColumnIndex("id"));
                result.leftBeginSign = ptr.getLong(ptr.getColumnIndex("leftBeginSign"));
                result.rightBeginSign = ptr.getLong(ptr.getColumnIndex("rightBeginSign"));
                result.leftHistoryTime = ptr.getInt(ptr.getColumnIndex("leftHistoryTime"));
                result.rightHistoryTime = ptr.getInt(ptr.getColumnIndex("rightHistoryTime"));
                result.leftTime = ptr.getInt(ptr.getColumnIndex("leftTime"));
                result.rightTime = ptr.getInt(ptr.getColumnIndex("rightTime"));
                result.isLeft = (ptr.getInt(ptr.getColumnIndex("isLeft")) == 0) ? false : true;
                result.isRight = (ptr.getInt(ptr.getColumnIndex("isRight")) == 0) ? false : true;
                String recordTimeStr = ptr.getString(ptr.getColumnIndex("recordTime"));
                result.recordTime = DateHelper.StringToDate(recordTimeStr);
            }
        }
        catch(Exception err)
        {
            result = null;
            System.out.printf(err.getMessage());
        }
        return result;
    }
}
