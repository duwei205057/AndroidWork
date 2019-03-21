package com.dw;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.os.Build;
import android.os.Bundle;
import android.os.Message;
import android.provider.Settings;
import android.support.annotation.RequiresApi;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

public class QuickAccessibilityService extends AccessibilityService {

    private static final boolean DEBUG = BuildConfig.DEBUG;

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    @Override
    protected void onServiceConnected() {
        AccessibilityServiceInfo serviceInfo = getServiceInfo();
//        if(serviceInfo == null) return;
//        String[] packNames = null;
//        serviceInfo.packageNames = packNames;
//        setServiceInfo(serviceInfo);
//        serviceInfo.eventTypes = AccessibilityEvent.TYPES_ALL_MASK;
//        serviceInfo.feedbackType = AccessibilityServiceInfo.FEEDBACK_ALL_MASK;
////        serviceInfo.feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC;
//        serviceInfo.notificationTimeout = 100;
//        setServiceInfo(serviceInfo);
//        packNames = new String[]{"com.dw.debug"};
//        serviceInfo.packageNames = packNames;
//        setServiceInfo(serviceInfo);
        super.onServiceConnected();
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return super.onUnbind(intent);
    }

    @Override
    public void onRebind(Intent intent) {
        super.onRebind(intent);
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR2)
            return;
        int eventType = event.getEventType();
        switch (eventType) {
            case AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED:
                break;
            case AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED:
                if (isSendPictureWindowShow(event)) {
                    int num = event.getSource().getChildCount();
                    AccessibilityNodeInfo sendButton = null;
                    for (int i = 0; i < num; i++) {
                        AccessibilityNodeInfo nodeInfo = event.getSource().getChild(i);
                        if("取消".equals(nodeInfo.getText())) {
                            sendButton = nodeInfo;
                            break;
                        }
                    }
                    if(sendButton != null && sendButton.isClickable()) {
//                        Rect rect = new Rect();
//                        sendButton.getBoundsInParent(rect);
//                        rect.offset(0, 50);
//                        sendButton.setBoundsInParent(rect);//java.lang.IllegalStateException: Cannot perform this action on a sealed instance.
                        sendButton.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                    }
                }
                break;
        }
    }

    private boolean isSendPictureWindowShow(AccessibilityEvent event) {
        if (event.getText() != null && event.getText().size() == 3) {
            return true;
        }
        return true;
    }

    @Override
    public void onInterrupt() {
        LOGD("onInterrupt");
    }

    @Override
    protected boolean onKeyEvent(KeyEvent event) {
        return super.onKeyEvent(event);
    }

    @Override
    protected boolean onGesture(int gestureId) {
        return super.onGesture(gestureId);
    }

    private static void LOGD(String message) {
        if(DEBUG) Log.d("xx",message);
    }

    public static boolean isAccessibilitySettingsOn(Context mContext, String serviceName) {
        int accessibilityEnabled = 0;
        // 对应的服务
        final String service = mContext.getPackageName() + "/" + serviceName;
        //Log.i(TAG, "service:" + service);
        try {
            accessibilityEnabled = Settings.Secure.getInt(mContext.getApplicationContext().getContentResolver(),
                    android.provider.Settings.Secure.ACCESSIBILITY_ENABLED);
            LOGD( "accessibilityEnabled = " + accessibilityEnabled);
        } catch (Settings.SettingNotFoundException e) {
            LOGD( "Error finding setting, default accessibility to not found: " + e.getMessage());
        }
        TextUtils.SimpleStringSplitter mStringColonSplitter = new TextUtils.SimpleStringSplitter(':');

        if (accessibilityEnabled == 1) {
            LOGD( "***ACCESSIBILITY IS ENABLED*** -----------------");
            String settingValue = Settings.Secure.getString(mContext.getApplicationContext().getContentResolver(),
                    Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES);
            if (settingValue != null) {
                mStringColonSplitter.setString(settingValue);
                while (mStringColonSplitter.hasNext()) {
                    String accessibilityService = mStringColonSplitter.next();

                    LOGD( "-------------- > accessibilityService :: " + accessibilityService + " " + service);
                    if (accessibilityService.equalsIgnoreCase(service)) {
                        LOGD( "We've found the correct setting - accessibility is switched on!");
                        return true;
                    }
                }
            }
        } else {
            LOGD( "***ACCESSIBILITY IS DISABLED***");
        }
        return false;
    }
}
