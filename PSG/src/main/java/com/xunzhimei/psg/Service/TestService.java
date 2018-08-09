package com.xunzhimei.psg.Service;

import android.annotation.TargetApi;
import android.app.Notification;
//import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.view.View;
import android.widget.Toast;

import com.xunzhimei.psg.Activieties.MainActivity;
import com.xunzhimei.psg.R;

public class TestService extends Service
{
    private Notification notification;

    @Override
    public IBinder onBind(Intent intent)
    {
        System.out.println("TestService-->" + "onBind" + "");
        // TODO: Return the communication channel to the service.
        return new Mybinder();
    }

    @Override
    public boolean onUnbind(Intent intent)
    {
        System.out.println("TestService-->" + "onUnbind" + "");
        return super.onUnbind(intent);
    }

    @Override
    public void onCreate()
    {


        super.onCreate();
    }

    public void sendChatMsg(View view)
    {

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        System.out.println("TestService-->" + "onStartCommand" + "");
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy()
    {
        System.out.println("TestService-->" + "onDestroy" + "");
        super.onDestroy();
    }

    public void Method_In_service(String string)
    {
        Toast.makeText(getApplicationContext(), "方法调用成功" + "Activity传入的值为：：" + string, Toast.LENGTH_SHORT).show();
    }

    //[2]在服务的内容定义一个内部类 extends 一个IBinder的子类Binder{IBinder类型}，子类内部包含我们想调用的方法
    public class Mybinder extends Binder
    {
        public void Method_Service_Binder(String string)
        {
            Method_In_service(string);
        }
    }
}
