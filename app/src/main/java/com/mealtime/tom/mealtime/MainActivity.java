package com.mealtime.tom.mealtime;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {

    private PowerManager.WakeLock _wakeLock;//安卓锁 程序中未使用
    private MealTimeDatabase _dbbase;//数据库操作类
    private Timer _timer;//时间轮询
    private boolean _isTimeActive = false;//时间轮询是否开始
    private MealCacheInfo _cacheInfo;//缓存信息
    private boolean _isCreate = false;//是否第一次运行程序
    //时间类型 UI界面使用
    private enum TimeType
    {
        Left,Right,Total
    }
    //时间轮询
    private Handler tHandler = new Handler(){
        public void handleMessage(Message msg) {
            switch (msg.what){
                case 0:
                    long curTime = new Date().getTime();
                    if(_cacheInfo.isLeft)
                    {
                        _cacheInfo.leftTime = (int)((curTime - _cacheInfo.leftBeginSign) / 1000);
                        SetTime(TimeType.Left, _cacheInfo.leftHistoryTime + _cacheInfo.leftTime);
                    }
                    if(_cacheInfo.isRight)
                    {
                        _cacheInfo.rightTime = (int)((curTime - _cacheInfo.rightBeginSign) / 1000);
                        SetTime(TimeType.Right, _cacheInfo.rightHistoryTime + _cacheInfo.rightTime);
                    }
                    int mealTotalTime = _cacheInfo.leftHistoryTime + _cacheInfo.leftTime + _cacheInfo.rightHistoryTime + _cacheInfo.rightTime;
                    SetTime(TimeType.Total, mealTotalTime);
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //Toast.makeText(MainActivity.this, "进入onCreate方法中", Toast.LENGTH_LONG).show();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        _dbbase = new MealTimeDatabase(this.getApplicationContext());
        InitUI();
        //test();//测试通过
        _isCreate = true;
    }


    protected void onStart() {
        super.onStart();
        //Toast.makeText(MainActivity.this, "onStart", Toast.LENGTH_LONG).show();
        if(_dbbase == null)
        {
            _dbbase = new MealTimeDatabase(this.getApplicationContext());
        }
        _cacheInfo = _dbbase.GetMealTimeCache();
        if(_cacheInfo == null)
        {
            _cacheInfo = new MealCacheInfo();
        }
        else
        {
            SetImageButton(TimeType.Left, _cacheInfo.isLeft);
            SetImageButton(TimeType.Right, _cacheInfo.isRight);
            if(_cacheInfo.isLeft || _cacheInfo.isRight)
            {
                _isTimeActive = true;
                TimerStart();
            }
        }
        if(_isCreate)
        {
            FillHistoryMealList(new Date(), 20);
        }
        else
        {
            SetIntervalTime();
        }
    }

    protected void onStop() {
        super.onStop();
        //Toast.makeText(MainActivity.this, "onStop", Toast.LENGTH_LONG).show();
        if(_isTimeActive)
        {
            _dbbase.AddMealTimeCache(_cacheInfo);
        }
        _isCreate = false;
    }

    @Override
    protected void onDestroy()
    {
        _cacheInfo.Clear();
        _dbbase.DeleteMealTimeCache();
        super.onDestroy();

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        switch(requestCode)
        {
            case 1://从页面MealInfoView返回
                MealInfo info = (MealInfo)data.getSerializableExtra("MealTimeInfo");
                LinearLayout list = findViewById(R.id.llMealList);
                switch(resultCode)
                {
                    case -1://删除
                        _dbbase.DelMealInfo(info.flowID);
                        for(int i = 0; i < list.getChildCount(); ++i)
                        {
                            LinearLayout item = (LinearLayout)list.getChildAt(i);
                            MealInfo itemInfo = (MealInfo)item.getTag();
                            if(info.flowID == itemInfo.flowID)
                            {
                                list.removeViewAt(i);
                                SetIntervalTime();
                                SetTodayMealCount();
                                break;
                            }
                        }
                        break;
                    case 0://返回
                        break;
                    case 1://保存
                        break;
                    case 2://更新
                        _dbbase.UpdateMealInfo(info);
                        for(int i = 0; i < list.getChildCount(); ++i)
                        {
                            LinearLayout item = (LinearLayout)list.getChildAt(i);
                            MealInfo itemInfo = (MealInfo)item.getTag();
                            if(info.flowID == itemInfo.flowID)
                            {
                                item.setTag(info);
                                String itemDateStr = String.valueOf(((TextView)item.getChildAt(0)).getText());
                                //设置修改后的信息
                                ((TextView)item.getChildAt(0)).setText(info.dateStr);
                                int leftMinute = MealInfo.SecondToMinute(info.leftTime);
                                ((TextView)item.getChildAt(1)).setText("左 " + leftMinute + "分");
                                int rightMinute = MealInfo.SecondToMinute(info.rightTime);
                                ((TextView)item.getChildAt(2)).setText("右 " + rightMinute + "分");
                                if(itemDateStr != info.dateStr)
                                {
                                    //日期改变，调整显示位置
                                    MealInfo[] sortInfos = _dbbase.GetMealInfos(0);
                                    if(sortInfos != null)
                                    {
                                        SetIntervalTime();
                                        SetTodayMealCount();
                                        for(int j = 0; j < sortInfos.length; ++j)
                                        {
                                            if(sortInfos[j].flowID == info.flowID)
                                            {
                                                int newIdx = sortInfos.length - j - 1;
                                                list.removeViewAt(i);
                                                list.addView(item, newIdx);
                                                break;
                                            }
                                        }
                                    }
                                }
                                break;
                            }
                        }
                        break;
                    default:
                        break;
                }
                break;
            default:
                break;
        }
    }

    private void test1()//日期添加后读取排序不对
    {
        MealInfo info = new MealInfo();
        info.dateStr = "2019-12-11 0:26";
        info.leftTime = 16;
        info.rightTime = 3;
        info.remark = "测试数据";
        _dbbase.AddMealInfo(info);
        info = new MealInfo();
        info.dateStr = "2019-12-11 4:7";
        info.leftTime = 0;
        info.rightTime = 1;
        info.remark = "测试数据";
        _dbbase.AddMealInfo(info);
        info = new MealInfo();
        info.dateStr = "2019-12-11 17:11";
        info.leftTime = 11;
        info.rightTime = 5;
        info.remark = "测试数据";
        _dbbase.AddMealInfo(info);
        info = new MealInfo();
        info.dateStr = "2019-12-11 20:22";
        info.leftTime = 12;
        info.rightTime = 12;
        info.remark = "测试数据";
        _dbbase.AddMealInfo(info);
        MealInfo[] infos = _dbbase.GetMealInfos(0);
        info = new MealInfo();
        info.dateStr = "2019-12-12 1:5";
        info.leftTime = 10;
        info.rightTime = 10;
        info.remark = "测试数据";
        _dbbase.AddMealInfo(info);
        infos = _dbbase.GetMealInfos(0);
        List<MealInfo> list = new ArrayList();
        for(int i = 0; i < infos.length; ++i)
        {
            list.add(infos[i]);
        }
        Collections.sort(list);

        int a = 1 + 1;

    }

    private void test()//测试通过
    {
        MealInfo[] infos = _dbbase.GetMealInfos(0);
        MealInfo info0 = new MealInfo();
        info0.dateStr = "2019-12-05 17:23";
        info0.leftTime = 111;
        info0.rightTime = 222;
        info0.remark = "测试数据";
        boolean isOK = false;
        int newId = _dbbase.AddMealInfo(info0);
        if(newId > 0)
        {
            MealInfo rInfo0 = _dbbase.GetMealInfo(3);
            if(rInfo0 != null)
            {
                rInfo0.dateStr = "2019-12-15 07:23";
                rInfo0.leftTime = 123;
                rInfo0.rightTime = 321;
                rInfo0.remark = "修改了测试数据";
                isOK = _dbbase.UpdateMealInfo(rInfo0);
                if(isOK)
                {
                    isOK = _dbbase.DelMealInfo(rInfo0.flowID);
                    if(isOK)
                    {
                        int a = 4 * 4;
                    }
                }
            }
        }
    }
    //UI初始化
    private void InitUI()
    {
        ImageButton btnLeft = findViewById(R.id.btnLeft);
        btnLeft.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v) {
                LeftEvent(v);
            }
        });
        ImageButton btnRight = findViewById(R.id.btnRight);
        btnRight.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v) {
                RightEvent(v);
            }
        });
        Button btnStop = findViewById(R.id.btnStop);
        btnStop.setVisibility(View.INVISIBLE);
        btnStop.setOnClickListener(new View.OnClickListener(){
            public void onClick(View view) {
                StopEvent(view);
            }
        });
        Button btnList = findViewById(R.id.btnList);
        btnList.setOnClickListener(new View.OnClickListener(){
            public void onClick(View view) {
                //列表放大
                LinearLayout part3 = findViewById(R.id.part3);
                LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams)part3.getLayoutParams();
                LinearLayout mealList = findViewById(R.id.llMealList);
                mealList.removeAllViews();
                if(lp.weight == 0)
                {
                    lp.weight = 0.8f;
                    FillHistoryMealList(new Date(), 20);
                }
                else
                {
                    lp.weight = 0;
                    FillHistoryMealList(new Date(), 0);
                }
                part3.setLayoutParams(lp);
            }
        });
        TextView tvInterval = findViewById(R.id.tvInterval);
        tvInterval.setOnClickListener(new View.OnClickListener(){
            public void onClick(View view) {
                SetIntervalTime();
            }
        });
        TextView tvIntervalLabel = findViewById(R.id.tvIntervalLabel);
        tvIntervalLabel.setOnClickListener(new View.OnClickListener(){
            public void onClick(View view) {
                SetIntervalTime();
            }
        });
    }
    //时间轮询开始
    private void TimerStart()
    {
        try
        {
            if(_timer == null)
            {
                _timer = new Timer();
            }
            _isTimeActive = true;
            TimerTask timeLoop = new TimerTask()
            {
                public void run() {
                    Message msg = tHandler.obtainMessage();
                    msg.what = 0;
                    tHandler.sendMessage(msg);
                }
            };
            _timer.schedule(timeLoop,1000,1000);
            Button btnStop = findViewById(R.id.btnStop);
            if(btnStop.getVisibility() == View.INVISIBLE)
            {
                btnStop.setVisibility(View.VISIBLE);
            }
        }
        catch(Exception err)
        {
            System.out.printf("\n"+ err.getMessage() +"\n");
        }
    }
    //时间轮询结束
    private void TimerStop()
    {
        _isTimeActive = false;
        if(_timer != null)
        {
            _timer.cancel();
            _timer = null;
        }
    }
    //设置时间数值，显示在界面中
    private void SetTime(TimeType type, int val)
    {
        int minute = val / 60;
        int second = val % 60;
        SetTime(type, minute, second);
    }
    //设置时间数值，显示在界面中
    private void SetTime(TimeType type, int minute, int second)
    {
        String minuteStr = TimeNumberToString(minute);
        String secondStr = TimeNumberToString(second);
        TextView tvMinute = null;
        TextView tvSecond = null;
        switch(type)
        {
            case Left:
                tvMinute = findViewById(R.id.tvLeftMinute);
                tvSecond = findViewById(R.id.tvLeftSecond);
                break;
            case Right:
                tvMinute = findViewById(R.id.tvRightMinute);
                tvSecond = findViewById(R.id.tvRightSecond);
                break;
            case Total:
                tvMinute = findViewById(R.id.tvMinute);
                tvSecond = findViewById(R.id.tvSecond);
                break;
        }
        try
        {
            tvMinute.setText(minuteStr);
            tvSecond.setText(secondStr);
        }
        catch(Exception err)
        {
            System.out.printf("\n" + err.getMessage() + "\n");
        }
    }
    //时间数值转换为字符串
    private String TimeNumberToString(int val)
    {
        String result;
        try
        {
            if(val >= 0 && val < 10)
            {
                result = "0" + val;
            }
            else if(val > 9 && val < 100)
            {
                result = String.valueOf(val);
            }
            else
            {
                result = "00";
            }
        }
        catch(Exception err)
        {
            result = "";
        }
        return result;
    }
    //批量创建用餐信息（向列表中添加用餐信息）
    private void CreateMealInfos(MealInfo[] infos)
    {
        if(infos != null)
        {
            for(int i = 0; i < infos.length; ++i)
            {
                CreateMealInfoItem(infos[i]);
            }
        }
    }
    //创建用餐信息（向列表中添加用餐信息）
    private void CreateMealInfoItem(MealInfo info)
    {
        LinearLayout layout = new LinearLayout(this.getApplicationContext());
        TextView tvDate = new TextView(this.getApplicationContext());
        TextView tvLeft = new TextView(this.getApplicationContext());
        TextView tvRight = new TextView(this.getApplicationContext());
        layout.setOrientation(LinearLayout.HORIZONTAL);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.MATCH_PARENT,1);
        lp.setMargins(0,5,0,5);
        layout.setLayoutParams(lp);
        layout.setTag(info);
        layout.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v) {
                ListItemClickEvent(v);
            }
        });

        tvDate.setTextSize(18);
        LinearLayout.LayoutParams dateLP = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,LinearLayout.LayoutParams.WRAP_CONTENT,1);
        dateLP.setMargins(25,0,0,0);
        tvDate.setLayoutParams(dateLP);
        tvDate.setText(info.dateStr);//todo:设置时间，可能格式有变动需要修改

        tvLeft.setTextSize(18);
        LinearLayout.LayoutParams leftLP = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,LinearLayout.LayoutParams.WRAP_CONTENT,1);
        leftLP.setMargins(0,0,0,0);
        tvLeft.setLayoutParams(leftLP);
        int leftMinute = MealInfo.SecondToMinute(info.leftTime);
        tvLeft.setText("左 " +leftMinute + "分");

        tvRight.setTextSize(18);
        LinearLayout.LayoutParams rightLP = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,LinearLayout.LayoutParams.WRAP_CONTENT,1);
        rightLP.setMargins(0,0,0,0);
        tvRight.setLayoutParams(rightLP);
        int rightMinute = MealInfo.SecondToMinute(info.rightTime);
        tvRight.setText("右 " + rightMinute + "分");

        LinearLayout list = findViewById(R.id.llMealList);
        layout.addView(tvDate);
        layout.addView(tvLeft);
        layout.addView(tvRight);
        list.addView(layout,0);
    }
    //设置左右图片按钮的图片
    private void SetImageButton(TimeType type, boolean isActive)
    {
        ImageButton btnLeft = findViewById(R.id.btnLeft);
        ImageButton btnRight = findViewById(R.id.btnRight);
        if(type == TimeType.Left)
        {

            if(isActive)
            {
                //btnLeft.setBackgroundResource(R.drawable.suckling);
                btnLeft.setImageResource(R.drawable.suckling);
                btnRight.setImageResource(R.drawable.right);
            }
            else
            {
                btnLeft.setImageResource(R.drawable.left);
            }
        }
        else if(type == TimeType.Right)
        {
            if(isActive)
            {
                btnRight.setImageResource(R.drawable.suckling);
                btnLeft.setImageResource(R.drawable.left);
            }
            else
            {
                btnRight.setImageResource(R.drawable.right);
            }
        }
    }
    //设置TextView的显示文本内容
    private void SetTextViewText(int id, String text)
    {
        TextView tvSum = findViewById(id);
        tvSum.setText(text);
    }
    //填充用餐信息并显示当天用餐数量
    private void FillHistoryMealList(Date todayDate, int topCount)
    {
        MealInfo[] infos = _dbbase.GetMealInfos(topCount);
        if(infos != null)
        {
            CreateMealInfos(infos);
        }
        SetIntervalTime();
        SetTodayMealCount();
    }
    //设置当天用餐次数
    private void SetTodayMealCount()
    {
        String todyaDateStr = DateHelper.DateToString(new Date());
        int mealCount = _dbbase.GetMealCountByDay(todyaDateStr);
        TextView tvTodaySum = findViewById(R.id.tvTodaySum);
        tvTodaySum.setText("今日用餐" + mealCount + "次");
    }

    //设置距离最后一次用餐间隔时间
    private void SetIntervalTime()
    {
        StringBuilder txt = new StringBuilder();
        if(_dbbase != null)
        {
            MealInfo lastMealInfo = _dbbase.GetLastMealInfo();
            if(lastMealInfo != null)
            {
                Date curDate = new Date();
                Date lastDate = DateHelper.StringToDate(lastMealInfo.dateStr);
                int interval = DateHelper.GetSecondBetweenDate(lastDate, curDate);
                int hour = interval / (60 * 60);
                int minuteVal = interval - hour * 60 * 60;
                int minute = minuteVal / 60;
                if(minuteVal % 60 > 0)
                {
                    minute += 1;
                }

                if(hour > 0)
                {
                    txt.append(hour);
                    txt.append("小时");
                }
                minute = (minute > 0) ? minute : 0;
                txt.append(minute);
                txt.append("分");
            }
            else
            {
                txt.append("0分");
            }
        }
        SetTextViewText(R.id.tvInterval, txt.toString());
    }

    //左侧按钮点击事件
    private void LeftEvent(View view)
    {
        if(_cacheInfo.isLeft)
        {
            _cacheInfo.isLeft = false;
            _cacheInfo.isRight = false;
            TimerStop();
            SetImageButton(TimeType.Left, false);
        }
        else
        {
            _cacheInfo.isLeft = true;
            _cacheInfo.isRight = false;
            Date curDate = new Date();
            _cacheInfo.leftBeginSign = curDate.getTime();
            _cacheInfo.leftHistoryTime += _cacheInfo.leftTime;
            SetImageButton(TimeType.Left, true);
            if(_cacheInfo.recordTime == null)
            {
                _cacheInfo.recordTime = curDate;
            }
            if(!_isTimeActive)
            {
                TimerStart();
            }
        }
    }
    //右侧按钮点击事件
    private void RightEvent(View view)
    {
        if(_cacheInfo.isRight)
        {
            _cacheInfo.isLeft = false;
            _cacheInfo.isRight = false;
            TimerStop();
            SetImageButton(TimeType.Right, false);
        }
        else
        {
            _cacheInfo.isLeft = false;
            _cacheInfo.isRight = true;
            Date curDate = new Date();
            _cacheInfo.rightBeginSign = curDate.getTime();
            _cacheInfo.rightHistoryTime += _cacheInfo.rightTime;
            SetImageButton(TimeType.Right, true);
            if(_cacheInfo.recordTime == null)
            {
                _cacheInfo.recordTime = curDate;
            }
            if(!_isTimeActive)
            {
                TimerStart();
            }
        }
    }
    //结束时间记录事件
    private void StopEvent(View view)
    {
        TimerStop();
        _cacheInfo.isLeft = false;
        _cacheInfo.isRight = false;
        SetImageButton(TimeType.Left, false);
        SetImageButton(TimeType.Right, false);
        MealInfo info = new MealInfo();
        info.dateStr = DateHelper.DateToString(_cacheInfo.recordTime);
        info.leftTime = _cacheInfo.leftHistoryTime + _cacheInfo.leftTime;
        info.rightTime = _cacheInfo.rightHistoryTime + _cacheInfo.rightTime;
        info.totalTime = info.leftTime + info.rightTime;
        int newId = _dbbase.AddMealInfo(info);
        if(newId > 0)
        {
            info.flowID = newId;
            CreateMealInfoItem(info);
            SetIntervalTime();
            SetTodayMealCount();//用餐次数
        }
        _cacheInfo.Clear();
        SetTime(TimeType.Left, 0);
        SetTime(TimeType.Right, 0);
        SetTime(TimeType.Total, 0);
        _dbbase.DeleteMealTimeCache();
        Button btn = (Button)view;
        btn.setVisibility(View.INVISIBLE);
    }
    //用餐信息列表中一条信息的点击事件
    private void ListItemClickEvent(View view)
    {
        MealInfo info = (MealInfo)view.getTag();
        Intent infoView = new Intent();
        infoView.putExtra("MealTimeInfo",info);
        infoView.setClass(MainActivity.this, MealTimeInfoView.class);
        startActivityForResult(infoView,1);
    }

    //获取电源锁，保持该服务在屏幕熄灭时仍然获取CPU时，保持运行
    @SuppressLint("InvalidWakeLockTag")
    private void AcquireWakeLock()
    {
        if (null == _wakeLock)
        {
            PowerManager pm = (PowerManager)this.getSystemService(Context.POWER_SERVICE);
            _wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK | PowerManager.ON_AFTER_RELEASE, "PostLocationService");
            if (null != _wakeLock)
            {
                _wakeLock.acquire();
            }
        }
    }

    //释放设备电源锁
    private void ReleaseWakeLock()
    {
        if (null != _wakeLock)
        {
            _wakeLock.release();
            _wakeLock = null;
        }
    }
}
