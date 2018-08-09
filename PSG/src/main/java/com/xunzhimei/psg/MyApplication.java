package com.xunzhimei.psg;

import android.app.Application;
import android.content.Context;

/**
 * Created by 周寒 on 2018/4/1.
 */

public class MyApplication extends Application
{
    private static Context mContext;

    public static Context getContext()
    {
        return mContext;
    }

    @Override
    public void onCreate()
    {
        super.onCreate();
        mContext = getApplicationContext();
    }
}
