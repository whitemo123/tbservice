package com.android.tbservice;

import android.accessibilityservice.AccessibilityServiceInfo;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.accessibility.AccessibilityManager;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_OVERLAY_PERMISSION = 101;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // root获取悬浮权限
        boolean overlayPermission = RootUtils.executeShellCommand("settings put global policy_control immersive.full=" + getPackageName());

        // root无障碍服务
        boolean accessibilityPermission = RootUtils.executeShellCommand("settings put secure enabled_accessibility_services " + getPackageName() + "/.TBAccessibilityService");

        // 判断悬浮窗权限
        if (overlayPermission || hasOverlayPermission(this)) {
            startService(new Intent(this, FloatingLogService.class));
        } else {
            requestOverlayPermission(MainActivity.this);
        }

        // 判断无障碍服务
        if (!accessibilityPermission && !isAccessibilitySettingsOn(this)) {
            Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
            startActivity(intent);
        }
    }

    /**
     * 判断悬浮窗权限
     *
     * @param context
     * @return
     */
    private boolean hasOverlayPermission(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return Settings.canDrawOverlays(context);
        } else {
            return true; // 对于 Android 6.0 以下的版本，默认返回true
        }
    }

    /**
     * 判断无障碍服务是否开启
     *
     * @param context
     * @return
     */
    private boolean isAccessibilitySettingsOn(Context context) {
        AccessibilityManager accessibilityManager = (AccessibilityManager) context.getSystemService(Context.ACCESSIBILITY_SERVICE);
        if (accessibilityManager != null) {
            for (AccessibilityServiceInfo service : accessibilityManager.getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_ALL_MASK)) {
                if (service.getResolveInfo().serviceInfo.packageName.equals(context.getPackageName()) &&
                        service.getResolveInfo().serviceInfo.name.equals("TBAccessibilityService")) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 跳转系统设置悬浮窗
     *
     * @param activity
     */
    public void requestOverlayPermission(Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + activity.getPackageName()));
            activity.startActivityForResult(intent, REQUEST_OVERLAY_PERMISSION);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_OVERLAY_PERMISSION) {
            if (hasOverlayPermission(this)) {
                // 用户已经授权悬浮窗权限，可以执行相关操作
                startService(new Intent(this, FloatingLogService.class));
            } else {
                // 用户未授权悬浮窗权限，可以提示用户开启权限
                requestOverlayPermission(MainActivity.this);
            }
        }
    }
}