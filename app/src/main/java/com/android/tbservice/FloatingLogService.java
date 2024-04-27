package com.android.tbservice;

import android.app.Service;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Handler;
import android.os.IBinder;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

public class FloatingLogService extends Service {

    private WindowManager windowManager;
    private View floatingBall;
    private View logListView;

    private WindowManager.LayoutParams ballParams;
    private WindowManager.LayoutParams logListParams;

    private boolean isLogListVisible = false;

    private int initialX;
    private int initialY;
    private float initialTouchX;
    private float initialTouchY;

    private ArrayList<String> logMessages = new ArrayList<>();

    private Logger logger;

    private int listernerId;

    public FloatingLogService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        // 创建悬浮球的视图
        floatingBall = LayoutInflater.from(this).inflate(R.layout.floating_ball_layout, null);

        // 创建悬浮日志列表的视图
        logListView = LayoutInflater.from(this).inflate(R.layout.floating_log_list_layout, null);

        ImageView floating_ball = floatingBall.findViewById(R.id.floating_ball);

        RecyclerView recyclerView = logListView.findViewById(R.id.log_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        LogAdapter adapter = new LogAdapter(logMessages);
        recyclerView.setAdapter(adapter);

        Button startBtn = logListView.findViewById(R.id.start);
        Button stopBtn = logListView.findViewById(R.id.stop);

        // 配置悬浮球的参数
        ballParams = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT);

        // 设置悬浮球的初始位置
        ballParams.gravity = Gravity.TOP | Gravity.START;
        ballParams.x = 0;
        ballParams.y = getWindow().heightPixels / 2;

        // 配置悬浮日志列表的参数
        logListParams = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT);

        // 设置悬浮日志列表的初始位置
        logListParams.gravity = Gravity.TOP | Gravity.START;
        logListParams.x = 0;
        logListParams.y = 0;
        logListParams.alpha = 0.9f;
        logListParams.windowAnimations = android.R.style.Animation_Dialog;

        // 获取 WindowManager 实例
        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);

        // 将悬浮球的视图添加到 WindowManager
        windowManager.addView(floatingBall, ballParams);

        logger = new Logger();

        listernerId = Logger.registerListener(new Logger.OnLogUpdateListener() {
            @Override
            public void onLogAdded(String log) {
                adapter.addLog(log);
                recyclerView.scrollToPosition(adapter.getItemCount() - 1);
            }
        });

        // 悬浮球点击事件
        floating_ball.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isLogListVisible) {
                    // 关闭悬浮日志列表
                    windowManager.removeView(logListView);
                    isLogListVisible = false;
                } else {
                    // 显示悬浮日志列表
                    windowManager.addView(logListView, logListParams);
                    isLogListVisible = true;
                }
            }
        });

        startBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                logger.addLog("开始执行");
            }
        });

        stopBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                logger.addLog("结束执行");
            }
        });

        // 悬浮球触摸事件
        floatingBall.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        initialX = ballParams.x;
                        initialY = ballParams.y;
                        initialTouchX = event.getRawX();
                        initialTouchY = event.getRawY();
                        return true;
                    case MotionEvent.ACTION_MOVE:
                        int offsetX = (int) (event.getRawX() - initialTouchX);
                        int offsetY = (int) (event.getRawY() - initialTouchY);
                        ballParams.x = initialX + offsetX;
                        ballParams.y = initialY + offsetY;
                        windowManager.updateViewLayout(floatingBall, ballParams);
                        return true;
                }
                return false;
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        // 在 Service 销毁时移除悬浮球和悬浮日志列表的视图
        if (floatingBall != null) {
            windowManager.removeView(floatingBall);
        }
        if (logListView != null) {
            windowManager.removeView(logListView);
        }

        Logger.unregisterListener(listernerId);
    }

    private DisplayMetrics getWindow() {
        WindowManager windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        DisplayMetrics displayMetrics = new DisplayMetrics();
        windowManager.getDefaultDisplay().getMetrics(displayMetrics);
        return displayMetrics;
    }
}
