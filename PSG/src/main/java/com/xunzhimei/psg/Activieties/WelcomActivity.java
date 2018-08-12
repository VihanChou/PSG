package com.xunzhimei.psg.Activieties;

import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;

import com.xunzhimei.psg.R;

public class WelcomActivity extends AppCompatActivity
{
    private boolean mIsTokenOk = true;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);
        hideBottomUIMenu();


        Thread thread = new Thread()
        {
            //创建子线程
            @Override
            public void run()
            {
                try
                {
                    sleep(1000);//使程序休眠1秒
                    if (mIsTokenOk)
                    {
                        Intent intent = new Intent(getApplicationContext(), MainActivity.class);//启动MainActivity
                        startActivity(intent);
                    }
                    else
                    {
                        Intent intent = new Intent(getApplicationContext(), LoginActivity.class);//启动MainActivity
                        startActivity(intent);
                    }
                    finish();//关闭当前活动
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
        };
        thread.start();//启动线程
    }

    //隐藏虚拟按键，并且全屏
    protected void hideBottomUIMenu()
    {
        //隐藏虚拟按键，并且全屏
        if (Build.VERSION.SDK_INT > 11 && Build.VERSION.SDK_INT < 19)
        { // lower api
            View v = this.getWindow().getDecorView();
            v.setSystemUiVisibility(View.GONE);
        }
        else if (Build.VERSION.SDK_INT >= 19)
        {
            //for new api versions.
            View decorView = getWindow().getDecorView();
            int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
            decorView.setSystemUiVisibility(uiOptions);
        }
    }

}
