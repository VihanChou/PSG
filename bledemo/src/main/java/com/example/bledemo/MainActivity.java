package com.example.bledemo;

import android.Manifest;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

import pub.devrel.easypermissions.EasyPermissions;


/**
 * Class  :MainActivity
 * Author :created by Vihan
 * Email  :vihanmy@google.com
 * Notes  :
 **/
public class MainActivity extends AppCompatActivity
{
    //Views
    private android.support.v7.widget.Toolbar mToolBar;
    private ImageView mIv_confiState;
    private TextView mTv_confiState;
    private SharedPreferences mSharedPreferences;

    //绑定服务,与服务进行通信
    private ServiceConnection mServiceConnection;
    private BLEService.channel2Activity mControlBle;
    private TextView mTv_Rssi;
    private int mRssi;
    private Thread mThread4ReadRssi;


    //-----------------------------------------------Activity固有方法-----------------------------------------
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mToolBar = (android.support.v7.widget.Toolbar) findViewById(R.id.tb);
        mToolBar.setFitsSystemWindows(true);
        setSupportActionBar(mToolBar);

        init();
        System.out.println("MainActivity-->" + "onCreate" + "");
        initViews();
        initUI();
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 10);
    }


    @Override
    public void finish()
    {
        Intent intent = new Intent(this, BLEService.class);
        unbindService(mServiceConnection);
        super.finish();
    }


    @Override
    protected void onStart()
    {
        initUI();
        super.onStart();
    }


    //-----------------------------------------------初始化-----------------------------------------

    private void init()
    {
        mSharedPreferences = getSharedPreferences(Constants.getSPFILENAME(), Context.MODE_PRIVATE);

        //开启服务   为了不让Activity绑定服务后解绑时候的服务销毁发生
        Intent in = new Intent(getApplicationContext(), BLEService.class);
        startService(in);
        System.out.println("MainActivity-->" + "init" + "");
        Intent intent = new Intent(this, BLEService.class);
        mServiceConnection = new ServiceConnection()
        {
            @Override
            public void onServiceConnected(ComponentName componentName, IBinder iBinder)
            {
                System.out.println("MainActivity-->" + "onServiceConnected" + "");
                mControlBle = (BLEService.channel2Activity) iBinder;
                if (mControlBle != null)
                {
                    System.out.println("MainActivity-->" + "onServiceConnected" + "");
                    initThread2GetRssi();
                    mThread4ReadRssi.start();  //在mThread4ReadRssi mControlBle 非空的情况下开启检测强度的线程
                }
                else
                {
                    System.out.println("MainActivity-->"+"onServiceConnected"+"null");
                }
            }

            @Override
            public void onServiceDisconnected(ComponentName componentName)
            {
                System.out.println("MainActivity-->" + "onServiceDisconnected" + "");
            }
        };
        bindService(intent, mServiceConnection, BIND_AUTO_CREATE);

    }

    private void initThread2GetRssi()
    {
        //该线程中方法：mBluetoothGatt.readRemoteRssi(); 的调用
        // 会触发蓝牙连接监听调用中的 onReadRemoteRssi 方法，以获取当前连接蓝牙的强度，
        // 可以从强度反映出蓝牙设备离手机的距离
        mThread4ReadRssi = new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                while (true)
                {
                    try
                    {
//                        System.out.println("run" + "================================");
                        Thread.sleep(100);                 //100ms更新一次 RSSI 数据
                        mRssi = mControlBle.doMethod_getRssi();

                        if (mRssi == 100)  //bad
                        {
                            runOnUiThread(new Runnable()
                            {
                                @Override
                                public void run()
                                {
                                    mTv_Rssi.setText("强度未知");
                                }
                            });
                        }
                        else
                        {
                            runOnUiThread(new Runnable()
                            {
                                @Override
                                public void run()
                                {
                                    mTv_Rssi.setText("信号强度：" + mRssi);
                                }
                            });
                        }
                    }
                    catch (InterruptedException e)
                    {
                        e.printStackTrace();
                    }
                }
            }
        });

    }

    private void initUI()
    {
        //设备配置提示框
        if (mSharedPreferences.getString(Constants.getMATCHDEVICE_MAC(), null) == null)
        {
            mTv_confiState.setText("  设备未绑定");
            mIv_confiState.setImageResource(R.drawable.ic_bluetooth_disabled_red_50dp);

        }
        else
        {
            mTv_confiState.setText("  设备已配置: " + mSharedPreferences.getString(Constants.getMATCHDEVICE_NAME(), null));
            mIv_confiState.setImageResource(R.drawable.ic_bluetooth_connected_black_24dp);
        }
    }

    private void initViews()
    {
        //获取ActionBar对象
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null)
        {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        mTv_confiState = (TextView) findViewById(R.id.tv_confiState);
        mTv_Rssi = (TextView) findViewById(R.id.tv_rssi);
        mIv_confiState = (ImageView) findViewById(R.id.iv_confiState);

    }

    //---------------------------------------------------------------蓝牙连接服务相关-----------------------------------------------------
    public void bt_con2ble(View view)
    {
        mControlBle.doMethod_Bleconnection();
    }

    public void bt_alarm(View view)
    {
        mControlBle.doMethod_BLEAlarm();
    }

    public void bt_disConnect(View view)
    {
        mControlBle.doMethod_Bledisconnect();
    }

    public void bt_disalarm(View view)
    {
        mControlBle.doMethod_BLECancleAlarm();
    }


    public void bt_getMAC(View view)
    {
        Intent intent = new Intent(getApplicationContext(), ScanBluActivity.class);
        startActivity(intent);
    }
}