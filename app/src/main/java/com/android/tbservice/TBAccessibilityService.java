package com.android.tbservice;

import android.accessibilityservice.AccessibilityService;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;

public class TBAccessibilityService extends AccessibilityService {

    private String TAG_NAME = "TBService";
    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        Log.i(TAG_NAME, "无障碍服务启动");
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        Log.i(TAG_NAME, event.toString());
    }

    @Override
    public void onInterrupt() {

    }
}
