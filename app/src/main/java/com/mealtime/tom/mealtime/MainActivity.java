package com.mealtime.tom.mealtime;

import android.content.Intent;
import android.os.Handler;
import android.os.Message;
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

import java.util.Calendar;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {

    private MealTimeDatabase _dbbase;
    private Timer _timer;
    private int _leftTime = 0;
    private int _rightTime = 0;
    private boolean _isLeft = false;
    private boolean _isRight = false;
    private boolean _isTimeActive = false;
    private Date _initDate;
    private int _mealCount = 0;

    private enum TimeType
    {
        Left,Right,Total
    }

    private Handler tHandler = new Handler(){
        public void handleMessage(Message msg) {
            switch (msg.what){
                case 0:
                    if(_isLeft)
                    {
                        ++_leftTime;
                        SetTime(TimeType.Left, _leftTime);
                    }
                    if(_isRight)
                    {
                        ++_rightTime;
                        SetTime(TimeType.Right, _rightTime);
                    }
                    SetTime(TimeType.Total, _leftTime + _rightTime);
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        _dbbase = new MealTimeDatabase(this.getApplicationContext());
        InitUI();
        //test();//测试通过
    }
    private void test()//测试通过
    {
        MealInfo[] infos = _dbbase.GetMealInfos();
        MealInfo info0 = new MealInfo();
        info0.dateStr = "2019-12-05 17:23";
        info0.leftTime = 111;
        info0.rightTime = 222;
        info0.remark = "测试数据";
        boolean isOK = _dbbase.AddMealInfo(info0);
        if(isOK == true)
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
                if(lp.weight == 0)
                {
                    lp.weight = 0.8f;
                }
                else
                {
                    lp.weight = 0;
                }
                part3.setLayoutParams(lp);
            }
        });
        MealInfo[] infos = _dbbase.GetMealInfos();
        _mealCount = 0;
        _initDate = new Date();
        if(infos != null)
        {
            CreateMealInfos(infos);
            for(int i = 0; i <  infos.length; ++i)
            {
                Date cmpDate = DateHelper.StringToDate(infos[i].dateStr);
                if(DateHelper.IsSameDate(_initDate,cmpDate, Calendar.DAY_OF_MONTH))
                {
                    _mealCount++;
                }
            }
        }
        TextView tvTodaySum = findViewById(R.id.tvTodaySum);
        tvTodaySum.setText("今日用餐" + _mealCount + "次");
    }

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

    private void TimerStop()
    {
        _isTimeActive = false;
        if(_timer != null)
        {
            _timer.cancel();
            _timer = null;
        }
    }

    private void SetTime(TimeType type, int val)
    {
        int hour = val / (60 * 60);
        int minute = val / 60;
        int second = val % 60;
        SetTime(type, hour, minute, second);
    }

    private void SetTime(TimeType type, int hour, int minute, int second)
    {
        String hourStr = TimeNumberToString(hour);
        String minuteStr = TimeNumberToString(minute);
        String secondStr = TimeNumberToString(second);
        TextView tvHour = null;
        TextView tvMinute = null;
        TextView tvSecond = null;
        switch(type)
        {
            case Left:
                tvHour = findViewById(R.id.tvLeftHour);
                tvMinute = findViewById(R.id.tvLeftMinute);
                tvSecond = findViewById(R.id.tvLeftSecond);
                break;
            case Right:
                tvHour = findViewById(R.id.tvRightHour);
                tvMinute = findViewById(R.id.tvRightMinute);
                tvSecond = findViewById(R.id.tvRightSecond);
                break;
            case Total:
                tvHour = findViewById(R.id.tvHour);
                tvMinute = findViewById(R.id.tvMinute);
                tvSecond = findViewById(R.id.tvSecond);
                break;
        }
        try
        {
            tvHour.setText(hourStr);
            tvMinute.setText(minuteStr);
            tvSecond.setText(secondStr);
        }
        catch(Exception err)
        {
            System.out.printf("\n" + err.getMessage() + "\n");
        }
    }

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

        tvDate.setTextSize(20);
        LinearLayout.LayoutParams dateLP = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,LinearLayout.LayoutParams.WRAP_CONTENT,1);
        dateLP.setMargins(25,0,0,0);
        tvDate.setLayoutParams(dateLP);
        tvDate.setText(info.dateStr);//todo:设置时间，可能格式有变动需要修改

        tvLeft.setTextSize(20);
        LinearLayout.LayoutParams leftLP = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,LinearLayout.LayoutParams.WRAP_CONTENT,1);
        leftLP.setMargins(0,0,0,0);
        tvLeft.setLayoutParams(leftLP);
        int leftMinute = info.leftTime / 60;
        tvLeft.setText("左侧 " +leftMinute + "分钟");

        tvRight.setTextSize(20);
        LinearLayout.LayoutParams rightLP = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,LinearLayout.LayoutParams.WRAP_CONTENT,1);
        rightLP.setMargins(0,0,0,0);
        tvRight.setLayoutParams(rightLP);
        int rightMinute = info.rightTime / 60;
        tvRight.setText("右侧 " + rightMinute + "分钟");

        LinearLayout list = findViewById(R.id.llMealList);
        layout.addView(tvDate);
        layout.addView(tvLeft);
        layout.addView(tvRight);
        list.addView(layout,0);
    }

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

    private void LeftEvent(View view)
    {
        if(_isLeft)
        {
            _isLeft = false;
            _isRight = false;
            TimerStop();
            SetImageButton(TimeType.Left, false);
        }
        else
        {
            _isLeft = true;
            _isRight = false;
            SetImageButton(TimeType.Left, true);
            if(!_isTimeActive)
            {
                TimerStart();
            }
        }
    }

    private void RightEvent(View view)
    {
        if(_isRight)
        {
            _isLeft = false;
            _isRight = false;
            TimerStop();
            SetImageButton(TimeType.Right, false);
        }
        else
        {
            _isLeft = false;
            _isRight = true;
            SetImageButton(TimeType.Right, true);
            if(!_isTimeActive)
            {
                TimerStart();
            }
        }
    }

    private void StopEvent(View view)
    {
        TimerStop();
        _isLeft = false;
        _isRight = false;
        SetImageButton(TimeType.Left, false);
        SetImageButton(TimeType.Right, false);
        MealInfo info = new MealInfo();
        Date curDate = new Date();
        Calendar cal = Calendar.getInstance();
        cal.setTime(curDate);
        int curYear = cal.get(Calendar.YEAR);
        int curMonth = cal.get(Calendar.MONTH) + 1;
        int curDay = cal.get(Calendar.DAY_OF_MONTH);
        int curHour = cal.get(Calendar.HOUR);
        int curMinute = cal.get(Calendar.MINUTE);
        info.dateStr = curYear + "-" + curMonth + "-" + curDay + " " + curHour + ":" + curMinute;
        info.leftTime = _leftTime;
        info.rightTime = _rightTime;
        boolean isSaveOk = _dbbase.AddMealInfo(info);
        if(isSaveOk)
        {
            CreateMealInfoItem(info);
            //计算用餐次数
            if(DateHelper.IsSameDate(_initDate, curDate, Calendar.DAY_OF_MONTH))
            {
                _mealCount++;
            }
            else
            {
                _initDate = curDate;
                _mealCount = 0;
            }
            TextView tvTodaySum = findViewById(R.id.tvTodaySum);
            tvTodaySum.setText("今日用餐" + _mealCount + "次");
            _leftTime = 0;
            _rightTime = 0;
            SetTime(TimeType.Left, _leftTime);
            SetTime(TimeType.Right, _rightTime);
            SetTime(TimeType.Total, 0);
            Button btn = (Button)view;
            btn.setVisibility(View.INVISIBLE);
        }
    }

    private void ListItemClickEvent(View view)
    {
        MealInfo info = (MealInfo)view.getTag();
        Intent infoView = new Intent();
        infoView.putExtra("MealTimeInfo",info);
        infoView.setClass(MainActivity.this, MealTimeInfoView.class);
        startActivityForResult(infoView,1);




        System.out.printf("\non click item: " + info.dateStr  + "\n");

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        switch(requestCode)
        {
            case 1:
                MealInfo info = (MealInfo)data.getSerializableExtra("MealTimeInfo");
                LinearLayout list = findViewById(R.id.llMealList);
                switch(resultCode)
                {
                    case -1:
                        _dbbase.DelMealInfo(info.flowID);
                        for(int i = 0; i < list.getChildCount(); ++i)
                        {
                            LinearLayout item = (LinearLayout)list.getChildAt(i);
                            MealInfo itemInfo = (MealInfo)item.getTag();
                            if(info.flowID == itemInfo.flowID)
                            {
                                list.removeViewAt(i);
                                break;
                            }
                        }
                        break;
                    case 0:
                        break;
                    case 1:
                        break;
                    case 2:
                        _dbbase.UpdateMealInfo(info);
                        for(int i = 0; i < list.getChildCount(); ++i)
                        {
                            LinearLayout item = (LinearLayout)list.getChildAt(i);
                            MealInfo itemInfo = (MealInfo)item.getTag();
                            if(info.flowID == itemInfo.flowID)
                            {
                                item.setTag(info);
                                //todo:设置修改后的信息
                                ((TextView)item.getChildAt(0)).setText(info.dateStr);
                                int leftMinute = info.leftTime / 60;
                                ((TextView)item.getChildAt(1)).setText("左侧 " + leftMinute + "分钟");
                                int rightMinute = info.rightTime / 60;
                                ((TextView)item.getChildAt(2)).setText("右侧 " + rightMinute + "分钟");
//                                for(int j = 0; j < item.getChildCount(); ++j)
//                                {
//                                    View itemChild = item.getChildAt(j);
//                                    int tmptype = itemChild.getLayerType();
//
//                                }
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
}
