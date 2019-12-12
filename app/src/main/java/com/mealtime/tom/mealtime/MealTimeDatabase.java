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
    private final static int _mealDbVersion = 8;

    public MealTimeDatabase(Context context)
    {
        super(context,_mealTableName,null,_mealDbVersion);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        CreateMealTable(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
    {
        MealInfo[] infos = GetMealInfos(db);
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
    public boolean AddMealInfo(MealInfo info)
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
            SQLiteDatabase db = getWritableDatabase();
            long newID = db.insert(_mealTableName, null, values);
            result = (newID > 0) ? true : false;
        }
        catch(Exception err)
        {
            result = false;
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
    public MealInfo[] GetMealInfos()
    {
        return GetMealInfos(getReadableDatabase());
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
    private MealInfo[] GetMealInfos(SQLiteDatabase db)
    {
        MealInfo[] result = null;
        try
        {
            Cursor bakData = db.query(_mealTableName,null,"",null,"","","datetime(date)","");
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
}
