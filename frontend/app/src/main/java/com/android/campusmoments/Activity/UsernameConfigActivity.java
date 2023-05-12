package com.android.campusmoments.Activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.android.campusmoments.R;
import com.android.campusmoments.Service.Services;

public class UsernameConfigActivity extends AppCompatActivity {
    public static final int SET_USERNAME_SUCCESS = 0;
    public static final int SET_USERNAME_FAIL = 1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_username_config);
        Services.setSetUsernameHandler(handler);
    }

    @SuppressLint("HandlerLeak")
    private final Handler handler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            if (msg.what == SET_USERNAME_SUCCESS) {
                successSetUsername();
            } else if (msg.what == SET_USERNAME_FAIL) {
                failSetUsername();
            }
        }
    };

    public void setUsername(View view) {
        TextView username_view = findViewById(R.id.username_person_center);
        String username = username_view.getText().toString();
        Services.setUsername(username);
    }

    private void successSetUsername() {
        Toast.makeText(this.getApplicationContext(), "更新成功", Toast.LENGTH_SHORT).show();
        finish();
    }

    private void failSetUsername() {
        Toast.makeText(this.getApplicationContext(), "更新失败", Toast.LENGTH_SHORT).show();
    }
    public void cancel(View view) {
        finish();
    }
}