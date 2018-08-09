package com.xunzhimei.psg;

import android.accessibilityservice.AccessibilityService;
import android.content.Intent;
import android.content.ServiceConnection;
import android.view.accessibility.AccessibilityEvent;

/**
 * Author: Created by 周寒 on 2018/8/7
 * Email : vihanmy@google.com
 * Notes :
 */
public class MyAccessibilityService extends AccessibilityService
{
    @Override
    public void onCreate()
    {
        new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                while (true)
                {
                }
            }
        }).start();
        super.onCreate();
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent accessibilityEvent)
    {

    }

    @Override
    public void onInterrupt()
    {

    }

    @Override
    public boolean bindService(Intent service, ServiceConnection conn, int flags)
    {
        return super.bindService(service, conn, flags);
    }

    @Override
    public boolean onUnbind(Intent intent)
    {
        return super.onUnbind(intent);
    }
}
