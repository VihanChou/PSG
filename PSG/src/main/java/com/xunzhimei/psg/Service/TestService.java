package com.xunzhimei.psg.Service;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationChannel;
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
        //安卓通知的版本适配
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O)
        {
            NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            notification = new NotificationCompat.Builder(this, "psg_ble_service")//此处的chat为我们定义的通知ID
                    .setContentTitle("安全服务正在运行")
                    .setContentText("您的安全，我来守候")
                    .setWhen(System.currentTimeMillis())
                    .setSmallIcon(R.drawable.ic_launcher)
                    .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.ic_launcher))
                    .build();
//            manager.notify(1, notification);
            //调用startForeground()方法就可以让MyService变成一个前台Service，并会将通知的图片显示出来。
            // 参数一：唯一的通知标识；参数二：通知消息。
        }
        else
        {
            Notification.Builder builder = null; //获取一个Notification构造器
            builder = new Notification.Builder(this.getApplicationContext());
            Intent nfIntent = new Intent(this, MainActivity.class);//点击通知的跳转Activity

            builder.setContentIntent(PendingIntent.getActivity(this, 0, nfIntent, 0)) // 设置PendingIntent
                    .setLargeIcon(BitmapFactory.decodeResource(this.getResources(),
                            R.mipmap.ic_launcher_round)) // 设置下拉列表中的图标(大图标)
                    .setContentTitle("安全服务正在运行") // 设置下拉列表里的标题
                    .setSmallIcon(R.mipmap.ic_launcher) // 设置状态栏内的小图标
                    .setContentText("您的安全，我来守候")      // 设置上下文内容
                    .setWhen(System.currentTimeMillis()); // 设置该通知发生的时间
            Notification notification = builder.build(); // 获取构建好的Notification
            notification.defaults = Notification.DEFAULT_SOUND; //设置为默认的声音

            //调用startForeground()方法就可以让MyService变成一个前台Service，并会将通知的图片显示出来。
            // 参数一：唯一的通知标识；参数二：通知消息。
//            startForeground(1, notification);
        }

        startForeground(1, notification);

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
