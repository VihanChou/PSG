
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

public class BLEService extends Service
{

    private static final String TAG = "BLEService";

    //蓝牙相关
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothManager mBluetoothManager;
    private BluetoothLeScanner mBluetoothLeScanner;
    private BluetoothGatt mBluetoothGatt;
    private boolean mIsBLEconnected = false;    //蓝牙是否连接标志
    private int mRssi;
    Handler mHandler = new Handler(Looper.getMainLooper());

    //时间差计算相关
    Date startDate = new Date();

    private SharedPreferences mSharedPreferences;
    private PushVideo mPushVideo;


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


    //连接当前配置的蓝牙
    public void bt_cconnect()
    {
        //获取蓝牙地址
        String macString = mSharedPreferences.getString(Constants.getMATCHDEVICE_MAC(), null);
        if (macString != null)
        {
            //连接逻辑
            // 蓝牙没有打开的时候，打开蓝牙
            if (!mBluetoothAdapter.isEnabled())
            {
                mBluetoothAdapter.enable();
            }
            // 获取到远程设备，
            final BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(macString.trim());
            if (mIsBLEconnected)
            {
                showToast("当前蓝牙已连接，请勿重复连接设备");
            }
            else
            {
                System.out.println("bt_cconnect" + "开始连接设备");
                // 开始连接，第二个参数表示是否需要自动连接，true设备靠近自动连接，第三个表示连接回调
                mBluetoothGatt = device.connectGatt(getApplicationContext(), false, mGattCallback);
            }
        }
        else
        {
            showToast("请先配置蓝牙设备");
        }

    }

    //监听连接回调
    private final BluetoothGattCallback mGattCallback = new BluetoothGattCallback()
    {
        // 连接状态变化
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState)
        {
            if (newState == BluetoothProfile.STATE_CONNECTED)// 连接上
            {
                boolean success = mBluetoothGatt.discoverServices(); // 去发现服务
                System.out.println("BLEService-->" + "onConnectionStateChange" + "连接成功");
                showToast("当前设备连接成功");
                mIsBLEconnected = true;
            }
            else if (newState == BluetoothProfile.STATE_DISCONNECTED)
            {
                // 连接断开
                System.out.println("BLEService-->" + "onConnectionStateChange" + "连接不稳定，已经断开");
                showToast("当前设备连接不稳定，已经断开");
                mIsBLEconnected = false;
            }
        }

        // 发现服务,在蓝牙连接的时候会调用
        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status)
        {
            if (status == BluetoothGatt.GATT_SUCCESS)
            {
                // 解析服务
                System.out.println("BLEService-->" + "onServicesDiscovered" + "");
                List<BluetoothGattService> list = mBluetoothGatt.getServices();
                for (BluetoothGattService bluetoothGattService : list)
                {
                    //某服务的UUID
                    String str = bluetoothGattService.getUuid().toString();
                    List<BluetoothGattCharacteristic> gattCharacteristics = bluetoothGattService.getCharacteristics();

                    BluetoothGattCharacteristic alertLevel = null;
                    for (BluetoothGattCharacteristic gattCharacteristic : gattCharacteristics)
                    {
                        if (Constants.getReceiBleDataCharecUuid().equals(gattCharacteristic.getUuid().toString()))
                        {
                            alertLevel = gattCharacteristic;
                            Log.e("onServicesDiscovered", alertLevel.getUuid().toString());
                        }
                    }
                    enableNotification(true, gatt, alertLevel);//必须要有，否则接收不到数据
                }
            }

            //该线程中方法：mBluetoothGatt.readRemoteRssi(); 的调用会，触发蓝牙连接监听调用中的onReadRemoteRssi方法，以获取当前连接蓝牙的强度，
            // 可以从强度反映出蓝牙设备离手机的距离
//            new Thread(new Runnable()
//            {
//                @Override
//                public void run()
//                {
//                    while (false)
//                    {
//                        try
//                        {
//                            System.out.println("run" + "================================");
//                            Thread.sleep(100);
//                            mBluetoothGatt.readRemoteRssi();
//                        }
//                        catch (InterruptedException e)
//                        {
//                            e.printStackTrace();
//                        }
//                    }
//                }
//            }).start();


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

    // 蓝牙接收使能
    private void enableNotification(boolean enable, BluetoothGatt gatt, BluetoothGattCharacteristic characteristic)
    {
        if (gatt == null || characteristic == null)
            return;
        gatt.setCharacteristicNotification(characteristic, enable);
    }

    //蓝牙发送信息，使智能硬件发声报警
    public void BLEAlarm()
    {
        if (mBluetoothGatt != null && mIsBLEconnected)
        {
            BluetoothGattService gattService = mBluetoothGatt.getService(UUID.fromString(Constants.getSendBleDataServiceUuid()));
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
        if (mBluetoothGatt != null && mIsBLEconnected)
        {
            BluetoothGattService gattService = mBluetoothGatt.getService(UUID.fromString(Constants.getSendBleDataServiceUuid()));
            BluetoothGattCharacteristic characteristic = gattService.getCharacteristic(UUID.fromString(Constants.getSendBleDataCharecUuid()));
            byte[] bytes = {0};
            characteristic.setValue(bytes);
            characteristic.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE);
            mBluetoothGatt.writeCharacteristic(characteristic);  //发送蓝牙数据
        }
    }

    //断开连接
    public void Bledisconnect()
    {
        if (mIsBLEconnected)
        {
            // 连接成功的GATT
            mBluetoothGatt.disconnect();
            mBluetoothGatt.close();
            mIsBLEconnected = false;
            System.out.println("Bledisconnect" + "当前蓝牙手动断开");
            showToast("当前蓝牙手动断开");

        }
        else
        {
            Toasty.info(getApplicationContext(), "当前没有蓝牙连接", Toast.LENGTH_SHORT).show();
        }
    }

    //---------------------------------------------------------------Binder相关供Activity调用---------------------------------------------------------
    //服务中的方法
    public void Method_In_service()
    {
//        Toast.makeText(getApplicationContext(), "方法调用成功", Toast.LENGTH_SHORT).show();
    }

    //服务中继承于Binder的类Mybinder
    public class channel2Activity extends Binder
    {
        public void doMethod_In_service()//内部类中的方法
        {
            Method_In_service();    //内部类中的方法调用服务中的方法
        }

        public void doMethod_Bleconnection()//内部类中的方法
        {
            bt_cconnect();
        }

        public void doMethod_Bledisconnect()//内部类中的方法
        {
            Bledisconnect();
        }

        public void doMethod_BLEAlarm()//内部类中的方法
        {
            BLEAlarm();
        }

        public void doMethod_startPush()//内部类中的方法
        {
            mPushVideo.startPush();
        }

        public void doMethod_stopPush()//内部类中的方法
        {
            mPushVideo.stopPush();
        }

        public void doMethod_BLECancleAlarm()//内部类中的方法
        {

            BLECancleAlarm();
        }

        public void doMethod_switchCamera()//内部类中的方法
        {
            mPushVideo.switchCamera();
        }


    }


    //------------------------------------------------工具-------------------------------------------------------------------------
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

}
