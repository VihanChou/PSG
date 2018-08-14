package com.xunzhimei.psg.Service;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.BluetoothLeScanner;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import com.xunzhimei.psg.Utils.Constants;
import com.xunzhimei.psg.Utils.PushVideo;

import java.util.Date;
import java.util.List;
import java.util.UUID;

import es.dmoral.toasty.Toasty;

/**
 * Class  :BLEService
 * Author :created by Vihan on 2018/8/14  16:03
 * Email  :vihanmy@google.com
 * Notes  :
 * 【1】关于蓝牙通信的条件
 * 蓝牙连接成功并不代表一定能够通信，
 * 只有蓝牙连接成功后，蓝牙服务也被找到的同时，才表示能够正常通信
 **/

public class BLEService extends Service
{
    private static final String TAG = "BLEService";
    int j = 0;
    //蓝牙相关
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothManager mBluetoothManager;
    private BluetoothLeScanner mBluetoothLeScanner;
    private BluetoothGatt mBluetoothGatt;
    private boolean mIsBLE_Connected = false;    //蓝牙是否连接标志
    private boolean mIsBLE_Finded = false;        //蓝牙服务是否查找到
    private boolean isFinding = false;        //蓝牙服务是否查找到

    private int mRssi;
    Handler mHandler = new Handler(Looper.getMainLooper());

    private int CAN_NOTR_EAD_RSSI = 100;

    //时间差计算相关
    Date startDate = new Date();

    private SharedPreferences mSharedPreferences;
    private PushVideo mPushVideo;
    private String macString;


    //-----------------------------------------生命周期方法---------------------------------------------
    public BLEService()
    {
        System.out.println("BLEService-->" + "BLEService" + "服务已经开启");
    }


    @Override
    public IBinder onBind(Intent intent)
    {
        // TODO: Return the communication channel to the service.
        return new channel2Activity();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        initBlueTooth();
        mPushVideo = new PushVideo();
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy()
    {
        System.out.println("BLEService-->" + "onDestroy" + "");
        super.onDestroy();
    }


    //------------------------------------蓝牙相关---------------------------------------------------------
    //蓝牙初始化
    private void initBlueTooth()
    {
        //1首先获取BluetoothManager
        mBluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        //2获取BluetoothAdapter,并打开蓝牙
        mBluetoothAdapter = mBluetoothManager.getAdapter();
        //蓝牙扫描类
        mBluetoothLeScanner = mBluetoothAdapter.getBluetoothLeScanner();

        //第一个参数是保存在哪一个xml文件中，第二个参数是操作模式，一般选择 MODE_PRIVATE
        mSharedPreferences = getSharedPreferences(Constants.getSPFILENAME(), Context.MODE_PRIVATE);
    }

    //监听连接回调
    private final BluetoothGattCallback mGattCallback = new BluetoothGattCallback()
    {
        // 连接状态变发生变化
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState)
        {
            System.out.println("BLEService-->" + "status：" + status);
            System.out.println("BLEService-->" + "newState：" + newState);

            if (newState == BluetoothProfile.STATE_CONNECTED)// 连接上
            {
                mIsBLE_Connected = true;
                System.out.println("BLEService-->" + "onConnectionStateChange" + "连接已准备");
                FindService();
            }
            else if (newState == BluetoothProfile.STATE_DISCONNECTED)
            {
                System.out.println("BLEService-->" + "onConnectionStateChange" + "蓝牙断开 status =  " + status);
                if (status == 8)
                {
                    // 连接断开
                    System.out.println("BLEService-->" + "onConnectionStateChange" + "连接不稳定，已经断开");
                    showToast("信号不稳定|连接已断开");
                }
                if (status == 133)
                {
                    // 连接断开
                    System.out.println("BLEService-->" + "onConnectionStateChange" + "蓝牙连接超时");
                    showToast("蓝牙连接超时");
                }
                isFinding = false;
                mIsBLE_Connected = false;
            }
        }

        // 发现服务,在蓝牙连接的时候会调用
        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status)
        {
            j++;
            System.out.println("BLEService-->" + "onServicesDiscovered" + "开始发现服务" + j);
            if (status == BluetoothGatt.GATT_SUCCESS && mIsBLE_Connected == true && mIsBLE_Finded == false)
            {
                BluetoothGattCharacteristic bluetoothGattCharacteristic = null;
                // 解析服务
                List<BluetoothGattService> list = mBluetoothGatt.getServices();
                for (BluetoothGattService bluetoothGattService : list)
                {
                    //某服务的UUID
                    String str = bluetoothGattService.getUuid().toString();
                    List<BluetoothGattCharacteristic> gattCharacteristics = bluetoothGattService.getCharacteristics();
//                    System.out.println("服务的UUID " + str);
                    for (BluetoothGattCharacteristic gattCharacteristic : gattCharacteristics)
                    {
                        bluetoothGattCharacteristic = gattCharacteristic;
//                        System.out.println("UUID " + bluetoothGattCharacteristic.getUuid().toString());
                        if (Constants.getReceiBleDataCharecUuid().equals(gattCharacteristic.getUuid().toString()))
                        {
                            bluetoothGattCharacteristic = gattCharacteristic;
                            Log.e("onServicesDiscovered", bluetoothGattCharacteristic.getUuid().toString());
                            enableNotification(true, gatt, bluetoothGattCharacteristic);//必须要有，否则接收不到数据
                            //蓝牙连接成功并能够通信的真正标志
                            showToast("蓝牙连接成功啦！！！！！");
                            mIsBLE_Finded = true;
                            return;
                        }
                    }
                }
            }
        }

        //特征读取变化
        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status)
        {
            if (status == BluetoothGatt.GATT_SUCCESS)
            {
                System.out.println("onCharacteristicRead" + "");
            }
        }

        // 收到数据时，该方法会被调用
        @Override
        public void onCharacteristicChanged(final BluetoothGatt gatt, BluetoothGattCharacteristic characteristic)
        {
            System.out.println("onCharacteristicChanged" + "----------------------------------------------");
            if (calLastedTime() < 900)
            {
                //TODO 双击逻辑 上传视频流
                mPushVideo.startPush();
                showToast("双击");
            }
            else
            {
                //TODO 单击逻辑
                showToast("单击");
            }
        }

        //调用  mBluetoothGatt.readRemoteRssi() 方法的时候该方法会被执行
        @Override
        public void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status)
        {
            System.out.println("onReadRemoteRssi" + "");
            mRssi = rssi;
            if (status == BluetoothGatt.GATT_SUCCESS)
            {
                //TODO 获取蓝牙信号强度
            }
            super.onReadRemoteRssi(gatt, rssi, status);
        }

    };

    //查找连接服务
    private void FindService()
    {
        System.out.println("BLEService-->" + "FindService" + ":" + mIsBLE_Finded);
        int i = 1000;
        while (i > 0)
        {
            if (!mIsBLE_Finded)  //如果服务发现失败
            {

                i--;
                if (mBluetoothGatt.discoverServices())
                {
                    System.out.println("BLEService-->" + "尝试次数:" + i);
                }
                else
                {
//                            System.out.println("BLEService-->" + "尝试次数:" + i);
                }
            }
            else //在1000次的尝试中，服务发现成功了
            {
                i = -1;
            }
        }

        new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                try
                {

                    Thread.sleep(2000);
                    if (mIsBLE_Finded == false)  //尝试一千次，并且停留2秒后服务发现仍然失败，那么就需要把之前的连接断开并提示用户
                    {
                        if (mIsBLE_Connected)
                        {
                            // 连接成功的GATT
                            mBluetoothGatt.disconnect();
                            mBluetoothGatt.close();
                            mIsBLE_Connected = false;
                        }
                        showToast("连接失败，请重试！");
                    }
                    else
                    {
//            showToast("连接成功！");
                    }
                    isFinding = false;

                }
                catch (InterruptedException e)
                {
                    e.printStackTrace();
                }
            }
        }).start();

    }

    // 蓝牙接收使能
    private void enableNotification(boolean enable, BluetoothGatt gatt, BluetoothGattCharacteristic characteristic)
    {
        if (gatt == null || characteristic == null)
        {
            return;
        }
        else
        {
            gatt.setCharacteristicNotification(characteristic, enable);
        }
    }

    //连接当前配置的蓝牙
    public void BLEConnect()
    {
        j = 0;
        //获取蓝牙地址
        macString = mSharedPreferences.getString(Constants.getMATCHDEVICE_MAC(), null);
        if (macString != null)
        {
            //连接逻辑
            // 蓝牙没有打开的时候，打开蓝牙
            if (!mBluetoothAdapter.isEnabled())
            {
                if (!mBluetoothAdapter.enable()) ;
                {
                    showToast("请打开手机蓝牙");
                    return;
                }
            }

            // 获取到远程设备，
            final BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(macString.trim());
            if (mIsBLE_Connected)
            {
                showToast("当前蓝牙已连接");
            }
            else
            {
                System.out.println("BLEConnect" + "开始连接设备");
                isFinding = true;
                // 开始连接，第二个参数表示是否需要自动连接，true设备靠近自动连接，第三个表示连接回调
                mBluetoothGatt = device.connectGatt(getApplicationContext(), false, mGattCallback);
            }
        }
        else
        {
            showToast("请先绑定蓝牙设备");
        }

    }

    //断开连接
    public void Bledisconnect()
    {
        if (isFinding)
        {
            showToast("请等待连接完成");
        }
        else
        {
            if (mIsBLE_Connected)
            {
                // 连接成功的GATT
                mBluetoothGatt.disconnect();
                mBluetoothGatt.close();
                mIsBLE_Connected = false;
                mIsBLE_Finded = false;
                System.out.println("Bledisconnect" + "当前蓝牙手动断开");
                showToast("蓝牙成功手动断开");

            }
            else
            {
                Toasty.info(getApplicationContext(), "当前没有蓝牙连接", Toast.LENGTH_SHORT).show();
            }
        }
    }


    //蓝牙发送信息，使智能硬件发声报警
    public void BLEAlarm()
    {
        if (mBluetoothGatt != null && mIsBLE_Connected && mIsBLE_Finded)
        {
            BluetoothGattService gattService = mBluetoothGatt.getService(UUID.fromString(Constants.getSendBleDataServiceUuid()));
            System.out.println("BLEService-->" + "BLEAlarm" + "--" + gattService);
            System.out.println("BLEService-->" + "BLEAlarm" + "--" + mBluetoothGatt);
            BluetoothGattCharacteristic characteristic = gattService.getCharacteristic(UUID.fromString(Constants.getSendBleDataCharecUuid()));
            byte[] bytes = {3};
            characteristic.setValue(bytes);
            characteristic.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE);
            mBluetoothGatt.writeCharacteristic(characteristic);  //发送蓝牙数据
            System.out.println("BLEService-->" + "BLEAlarm" + "");
        }
    }

    //蓝牙发送信息，使智能硬件取消发声报警
    public void BLECancleAlarm()
    {
        if (mBluetoothGatt != null && mIsBLE_Connected && mIsBLE_Finded)
        {
            BluetoothGattService gattService = mBluetoothGatt.getService(UUID.fromString(Constants.getSendBleDataServiceUuid()));
            BluetoothGattCharacteristic characteristic = gattService.getCharacteristic(UUID.fromString(Constants.getSendBleDataCharecUuid()));
            byte[] bytes = {0};
            characteristic.setValue(bytes);
            characteristic.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE);
            mBluetoothGatt.writeCharacteristic(characteristic);  //发送蓝牙数据
        }
    }


    //---------------------------------------------------------------Binder相关供Activity调用---------------------------------------------------------
    public void methodInService()//内部类中的方法
    {
        System.out.println("BLEService-->" + "methodInService" + " mBluetoothGatt.readRemoteRssi();" + mBluetoothGatt.readRemoteRssi());
    }

    //服务中继承于Binder的类Mybinder
    public class channel2Activity extends Binder
    {


        public void doMethod_methodInService()//内部类中的方法
        {
            methodInService();
        }

        public void doMethod_Bleconnection()//蓝牙连接
        {
            BLEConnect();
        }

        public void doMethod_Bledisconnect()//断开连接
        {
            Bledisconnect();
        }

        public void doMethod_BLEAlarm()//硬件发声报警
        {
            BLEAlarm();
        }

        public void doMethod_BLECancleAlarm()//取消硬件发声报警
        {
            BLECancleAlarm();
        }

        public void doMethod_startPush()//开始推流
        {
            mPushVideo.startPush();
        }

        public void doMethod_stopPush()//停止推流
        {
            mPushVideo.stopPush();
        }

        public void doMethod_switchCamera()//换摄像头
        {
            mPushVideo.switchCamera();
        }

        public int doMethod_getRssi()//返回真：可以进行 继续RSSi获取工作。返回假：不能进行RSSI获取工作
        {
            if (mIsBLE_Connected && mIsBLE_Finded && mBluetoothGatt.readRemoteRssi())
            {
                return mRssi;
            }
            else
            {
                return CAN_NOTR_EAD_RSSI;
            }
        }

    }

    //-----------------------------------------------------------------------工具-------------------------------------------------------------------------
    private void showToast(final String message2Show)
    {
        mHandler.post(new Runnable()
        {
            public void run()
            {
                Toasty.info(getApplicationContext(), message2Show, Toast.LENGTH_SHORT).show();
            }
        });
    }

    //时间差计算工具，判断单击双击
    public int calLastedTime()
    {
        long a = new Date().getTime();
        long b = startDate.getTime();
        int c = (int) ((a - b));
        startDate.setTime(a);
        return c;
    }

    //scanRecords的格式转换
    static final char[] hexArray = "0123456789ABCDEF".toCharArray();

    private static String bytesToHex(byte[] bytes)
    {
        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++)
        {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }

    //该线程中方法：mBluetoothGatt.readRemoteRssi(); 的调用
    // 会触发蓝牙连接监听调用中的 onReadRemoteRssi 方法，以获取当前连接蓝牙的强度，
    // 可以从强度反映出蓝牙设备离手机的距离
//    Thread thread = new Thread(new Runnable()
//    {
//        @Override
//        public void run()
//        {
//            while (true)
//            {
//                try
//                {
//                    System.out.println("run" + "================================");
//                    Thread.sleep(100);  //100ms更新一次 RSSI 数据
//                    if (mBluetoothGatt != null)
//                    {
//                        mBluetoothGatt.readRemoteRssi();
//                    }
//                }
//                catch (InterruptedException e)
//                {
//                    e.printStackTrace();
//                }
//            }
//        }
//    });
}
