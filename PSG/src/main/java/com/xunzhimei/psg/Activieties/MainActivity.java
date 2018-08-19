package com.xunzhimei.psg.Activieties;

import android.Manifest;

import android.app.AlertDialog;
//import android.app.NotificationChannel;

import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;

import android.os.IBinder;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.xunzhimei.psg.R;
import com.xunzhimei.psg.Service.BLEService;
import com.xunzhimei.psg.Utils.Constants;

import java.util.List;

import pub.devrel.easypermissions.EasyPermissions;

/**
 * Class  :MainActivity
 * Author :created by Vihan
 * Email  :vihanmy@google.com
 * Notes  :
 **/
public class MainActivity extends AppCompatActivity implements EasyPermissions.PermissionCallbacks
{
    //Views
    private android.support.v7.widget.Toolbar mToolBar;
    private boolean mBoot_Flag = false;
    private Button mBt_bootCheck;
    private ImageView mIv_confiState;
    private TextView mTv_confiState;
    private SharedPreferences mSharedPreferences;

    //绑定服务,与服务进行通信
    private ServiceConnection mServiceConnection;
    private BLEService.channel2Activity mControlBle;
    private TextView mTv_Rssi;
    private int mRssi;
    private Thread mThread4ReadRssi;


    //动态权限申请
    private String[] permissions = {
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.CAMERA};
    private final int REQUEST_ENABLE_PERMISSION = 10;


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
        initViews();
        initUI();
        askPermission();

//        SpeciaPermission();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_ENABLE_PERMISSION)
        {
            if (!EasyPermissions.hasPermissions(this, permissions))
            {

                EasyPermissions.requestPermissions(this, "请打开所有权限，否则程序将无法正常运行",
                        REQUEST_ENABLE_PERMISSION, permissions);
            }
            else
            {
                Toast.makeText(this, "已授权", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void finish()
    {
        Intent intent = new Intent(this, BLEService.class);
        unbindService(mServiceConnection);
        super.finish();
    }


    //该方法用于向  setSupportActionBar(mToolBar);  设置的ToolBar中设置菜单
    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(R.menu.menu_4_mainactivity, menu);
        return true;
    }

    //Actionbar  点击事件
    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId())
        {
            case R.id.bt_ScanBlu:
                Intent intent = new Intent(getApplicationContext(), ScanBluActivity.class);
                startActivity(intent);
                break;

            case R.id.bt_seting:
                Intent ntent = new Intent(getApplicationContext(), SettingActivity.class);
                startActivity(ntent);
                break;
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    protected void onStart()
    {
        initUI();
        super.onStart();
    }


    //-----------------------------------------------初始化-----------------------------------------

//    @TargetApi(Build.VERSION_CODES.O)
//    private void createNotificationChannel(String channelId, String channelName, int importance)
//    {
//        NotificationChannel channel = new NotificationChannel(channelId, channelName, importance);
//        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
//        notificationManager.createNotificationChannel(channel);
//    }

    private void init()
    {
        //第一个参数是保存在哪一个xml文件中，第二个参数是操作模式，一般选择 MODE_PRIVATE
        mSharedPreferences = getSharedPreferences(Constants.getSPFILENAME(), Context.MODE_PRIVATE);

        //开启服务   为了不让Activity绑定服务后解绑时候的服务销毁发生
        Intent in = new Intent(getApplicationContext(), BLEService.class);
        startService(in);

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
                    initThread2GetRssi();
                    mThread4ReadRssi.start();  //在mThread4ReadRssi mControlBle 非空的情况下开启检测强度的线程
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

    //-----------------------------------------------------权限申请相关----------------------------------------------------------------
    public void askPermission()
    {

        //当手机安卓版本大于等于23时，才有必要去判断权限是否去获取
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
        {
            //权限是否授权
            if (!EasyPermissions.hasPermissions(this, permissions))
            {
                EasyPermissions.requestPermissions(this, "请打开所有必要的权限",
                        REQUEST_ENABLE_PERMISSION, permissions);
            }
            else
            {
                System.out.println("MainActivity-->" + "askPermission" + "已授权");
            }
        }

    }

    private void SpeciaPermission()
    {
        Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
        intent.setData(Uri.parse("package:" + getPackageName()));
        startActivityForResult(intent, 100);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults)
    {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    //下面两个方法是实现EasyPermissions的EasyPermissions.PermissionCallbacks接口
    //分别返回授权成功和失败的权限
    @Override
    public void onPermissionsGranted(int requestCode, List<String> perms)
    {
        Log.i("ble", "获取成功的权限" + perms);
    }

    @Override
    public void onPermissionsDenied(int requestCode, List<String> perms)
    {
        if (EasyPermissions.somePermissionPermanentlyDenied(this, perms))
        {

            new AlertDialog.Builder(this)
                    .setTitle("请打开所有必要权限")
                    .setMessage("拒绝开启权限可能会导致程序某些功能不能正常使用，点击确认进入权限设置界面，点击退出则退出程序")
                    .setPositiveButton("确定", new DialogInterface.OnClickListener()
                    {
                        @Override
                        public void onClick(DialogInterface dialog, int which)
                        {
                            Uri packageURI = Uri.parse("package:" + MainActivity.this.getPackageName());
                            Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, packageURI);
                            startActivityForResult(intent, REQUEST_ENABLE_PERMISSION);
                        }
                    })
                    .setNegativeButton("退出", new DialogInterface.OnClickListener()
                    {
                        @Override
                        public void onClick(DialogInterface dialog, int which)
                        {
                            finish();
                        }
                    }).setCancelable(false).show();
        }
        else
        {
            if (!EasyPermissions.hasPermissions(this, permissions))
            {

                EasyPermissions.requestPermissions(this, "请打开所有权限，否则程序将无法正常运行",
                        REQUEST_ENABLE_PERMISSION, permissions);
            }
            else
            {
                Toast.makeText(this, "已授权", Toast.LENGTH_SHORT).show();
            }
        }

        Log.i("ble", "获取失败的权限" + perms);
    }

    //---------------------------------------------------------------蓝牙连接，推流,服务相关-----------------------------------------------------
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

    public void bt_stopPush(View view)
    {
        mControlBle.doMethod_stopPush();
    }

    public void bt_startPush(View view)
    {
        mControlBle.doMethod_startPush();
    }

    public void bt_test(View view)
    {
        mControlBle.doMethod_methodInService();
    }

}