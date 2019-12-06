package com.mealtime.tom.mealtime;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import java.io.Serializable;

public class MealTimeInfoView extends AppCompatActivity {

    private MealInfo _info;
    private int _resultCode = 0;//-1：删除；0：返回；1：保存；2：修改

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_meal_time_info_view);
        InitUI();
        Intent intent = getIntent();
        Serializable sData = intent.getSerializableExtra("MealTimeInfo");
        if(sData != null)
        {
            _info = (MealInfo)sData;
            SetInfo(_info);
        }
    }

    @Override
    public void onBackPressed()
    {
        Intent intent = new Intent();
        if(_resultCode != 0)
        {
            GetInfo();
            intent.putExtra("MealTimeInfo",_info);
        }
        setResult(_resultCode,intent);
        finish();
    }

    private void InitUI()
    {
        Button btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v) {
                _resultCode = 0;
                onBackPressed();
            }
        });
        Button btnSave = findViewById(R.id.btnSave);
        btnSave.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v) {
                if(_info != null)
                {
                    _resultCode = 2;
                    onBackPressed();
                }
            }
        });
        Button btnDel = findViewById(R.id.btnDelete);
        btnDel.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v) {
                _resultCode = -1;
                onBackPressed();
            }
        });
    }

    private void SetInfo(MealInfo info)
    {
        EditText etDate = findViewById(R.id.etDate);
        etDate.setText(info.dateStr);
        EditText etLeft = findViewById(R.id.etLeft);
        int leftMinute = info.leftTime / 60;
        etLeft.setText(String.valueOf(leftMinute));
        EditText etRight = findViewById(R.id.etRight);
        int rightMinute = info.rightTime / 60;
        etRight.setText(String.valueOf(rightMinute));
        EditText etRemark = findViewById(R.id.etRemark);
        etRemark.setText(info.remark);
    }

    private MealInfo GetInfo()
    {
        EditText etDate = findViewById(R.id.etDate);
        _info.dateStr = String.valueOf(etDate.getText());
        EditText etLeft = findViewById(R.id.etLeft);
        _info.leftTime = Integer.valueOf(String.valueOf(etLeft.getText())) * 60;
        EditText etRight = findViewById(R.id.etRight);
        _info.rightTime = Integer.valueOf(String.valueOf(etRight.getText())) * 60;
        EditText etRemark = findViewById(R.id.etRemark);
        _info.remark = String.valueOf(etRemark.getText());
        return _info;
    }
}
