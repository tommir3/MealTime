package com.mealtime.tom.mealtime;

import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {

    private Timer _timer;
    private int _leftTime = 0;
    private int _rightTime = 0;
    private boolean _isLeft = false;
    private boolean _isRight = false;
    private boolean _isTimeActive = false;

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

    private TimerTask OnTimerLoop = new TimerTask()
    {
        public void run() {
            Message msg = tHandler.obtainMessage();
            msg.what = 0;
            tHandler.sendMessage(msg);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        InitUI();
    }

    private void InitUI()
    {
        _timer = new Timer();
        _timer.schedule(OnTimerLoop,1000,1000);
        OnTimerLoop.cancel();
        ImageButton btnLeft = findViewById(R.id.btnLeft);
        btnLeft.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v) {
                System.out.printf("\nleft is active\n");
                if(_isLeft)
                {
                    _isLeft = false;
                    _isRight = false;
                    TimerStop();
                }
                else
                {
                    _isLeft = true;
                    _isRight = false;
                    if(!_isTimeActive)
                    {
                        TimerStart();
                    }
                }
            }
        });
        ImageButton btnRight = findViewById(R.id.btnRight);
        btnRight.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v) {
                System.out.printf("\nright is active\n");
                if(_isRight)
                {
                    _isLeft = false;
                    _isRight = false;
                    TimerStop();
                }
                else
                {
                    _isLeft = false;
                    _isRight = true;
                    if(!_isTimeActive)
                    {
                        TimerStart();
                    }
                }
            }
        });
    }

    private void TimerStart()
    {
        if(_timer == null)
        {
            _timer = new Timer();
        }
        _isTimeActive = true;
        try
        {
            //_timer.schedule(OnTimerLoop,1000,1000);
            OnTimerLoop.run();
        }
        catch(Exception err)
        {
            System.out.printf("\n"+ err.getMessage() +"\n");
        }
    }

    private void TimerStop()
    {
        _isTimeActive = false;
        OnTimerLoop.cancel();
        //_timer = null;
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
}
