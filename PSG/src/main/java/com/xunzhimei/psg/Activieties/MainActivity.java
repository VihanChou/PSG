package com.xunzhimei.psg.Activieties;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.xunzhimei.psg.R;
import com.xunzhimei.psg.Utils.Constants;

import java.util.Date;
import java.util.List;

import pub.devrel.easypermissions.AppSettingsDialog;
import pub.devrel.easypermissions.EasyPermissions;

public class MainActivity extends AppCompatActivity implements EasyPermissions.PermissionCallbacks
{
    //时间差计算相关
    Date startDate = new Date();

    //Views
    private android.support.v7.widget.Toolbar mToolBar;
    private boolean mBoot_Flag = false;
    private Button mBt_bootCheck;
    private ImageView mIv_confiState;
    private TextView mTv_confiState;
    public static final int WRITE_EXTERNAL_STORAGE = 100;

    private SharedPreferences mSharedPreferences;
    private String[] mPerms = {Manifest.permission.ACCESS_NOTIFICATION_POLICY};
    private String mNowPermissioning;

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
        System.out.println("onCreate----------" + startDate.getTime());
    }

    @TargetApi(Build.VERSION_CODES.O)
    private void createNotificationChannel(String channelId, String channelName, int importance)
    {
        NotificationChannel channel = new NotificationChannel(channelId, channelName, importance);
        NotificationManager notificationManager = (NotificationManager) getSystemService(
                NOTIFICATION_SERVICE);
        notificationManager.createNotificationChannel(channel);
    }

    private void init()
    {
        //第一个参数是保存在哪一个xml文件中，第二个参数是操作模式，一般选择 MODE_PRIVATE
        mSharedPreferences = getSharedPreferences(Constants.getSPFILENAME(), Context.MODE_PRIVATE);

        //通知版本适配，创建通知渠道
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O)
        {
            //创建了两个通知渠道
            String channelId = "psg_ble_service";
            String channelName = "安全服务通知";
            int importance = NotificationManager.IMPORTANCE_HIGH;
            createNotificationChannel(channelId, channelName, importance);

            channelId = "subscribe";
            channelName = "订阅消息";
            importance = NotificationManager.IMPORTANCE_DEFAULT;
            createNotificationChannel(channelId, channelName, importance);

        }
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

        mBt_bootCheck = (Button) findViewById(R.id.bt_bootcheck);
        mTv_confiState = (TextView) findViewById(R.id.tv_confiState);
        mIv_confiState = (ImageView) findViewById(R.id.iv_confiState);
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

            case R.id.bt_Help:
                Intent ntent = new Intent(getApplicationContext(), ScanBluActivity.class);
                startActivity(ntent);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    public void bt_BootCheck(View view)
    {
        if (mBoot_Flag)
        {
            mBt_bootCheck.setBackground(getDrawable(R.drawable.mainbuttonshape));
        }
        else
        {
            mBt_bootCheck.setBackground(getDrawable(R.drawable.mainbuttonshapegreen));
        }
        mBoot_Flag = !mBoot_Flag;

    }

    @Override
    protected void onStart()
    {
        initUI();
        super.onStart();
    }


    public void bt_test(View view)
    {
        String[] params = {Manifest.permission.WRITE_EXTERNAL_STORAGE};
        mNowPermissioning = params[0];


        if (EasyPermissions.hasPermissions(this, mNowPermissioning))
        {
            System.out.println("MainActivity-->" + "bt_test" + "权限获取成功");
        }
        else
        {
            EasyPermissions.requestPermissions(this, "需要读写本地权限", WRITE_EXTERNAL_STORAGE, mNowPermissioning);
        }

    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults)
    {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    @Override
    public void onPermissionsGranted(int requestCode, List<String> perms)
    {
        //如果checkPerm方法，没有注解AfterPermissionGranted，也可以在这里调用该方法。
    }

    @Override
    public void onPermissionsDenied(int requestCode, List<String> perms)
    {

        //当用户不允许在当前界面提示快捷获取权限方式的时候
        if (EasyPermissions.somePermissionPermanentlyDenied(this, perms))
        {
            //这里需要重新设置Rationale和title，否则默认是英文格式
            new AppSettingsDialog.Builder(this)
                    .setRationale("没有该权限，此应用程序无法正常工作。点击确认，打开应用设置界面以修改应用权限，点击取消，退出软件")
                    .setTitle("您需要让我们的App具有以下权限" + perms)
                    .build()
                    .show();
        }
        else//用户仅仅拒绝了权限
        {
            //再次申请权限
            EasyPermissions.requestPermissions(this, "需要读写本地权限", WRITE_EXTERNAL_STORAGE, mNowPermissioning);
        }

    }


    public void bt_getPermission(View view)
    {
//        dialog.show();
//        normalDialog.show();

    }
}



