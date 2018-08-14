package com.xunzhimei.psg.Activieties;

import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.xunzhimei.psg.R;
import com.xunzhimei.psg.Utils.Constants;


import java.util.ArrayList;
import java.util.List;

import es.dmoral.toasty.Toasty;

/**
 * Class  :ScanBluActivity
 * Author :created by Vihan on 2018/8/12  17:46
 * Email  :vihanmy@google.com
 * Notes  :为防止用户蓝牙关闭导致的 蓝牙打开失败空指针崩溃
 * A：蓝牙权限被拒绝访问时，采用以下逻辑
 * 【1】:用户在蓝牙未打开时进入该页面，直接finish该Activity，并弹出Toast
 * 【2】:扫描过程中若用户关闭蓝牙，扫描5秒完成会提示用户蓝牙已经被手动关闭
 * 【3】:在第二种情况下，用户再次下拉刷新扫描，会提示蓝牙未打开
 **/

public class ScanBluActivity extends AppCompatActivity
{
    //蓝牙相关
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothManager mBluetoothManager;
    private BluetoothLeScanner mBluetoothLeScanner;
    private ArrayList<BluetoothDevice> mLeDevices = new ArrayList<BluetoothDevice>();
    private ArrayList<Integer> mRSSIs = new ArrayList<Integer>();
    private ArrayList<byte[]> mRecords = new ArrayList<byte[]>();

    //View 相关
    private Toolbar mTb;
    private RecyclerView mRv;
    private LayoutInflater mInflat;
    private recyclerViewAdapter mAdapter;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private SwipeRefreshLayout.OnRefreshListener mRefreshListener;

    private SharedPreferences mSharedPreferences;
    private ScanCallback mScanCallback = null;


    //
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan_blu);

        initBlueTooth();
        initView();
        init();
        new Handler().postDelayed(new Runnable()
        {
            @Override
            public void run()
            {
                mSwipeRefreshLayout.setRefreshing(true);
                mRefreshListener.onRefresh();
            }
        }, 100);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        if (item.getItemId() == android.R.id.home)
        {
            ScanBluActivity.this.finish();
        }
        return true;
    }

    //
    private void init()
    {
        mScanCallback = new ScanCallback()
        {
            //发现一个广播
            @Override
            public void onScanResult(int callbackType, ScanResult result)
            {
                byte[] scanRecord = result.getScanRecord().getBytes();
                BluetoothDevice bluetoothDevice = result.getDevice();
                int rssi = result.getRssi();

                if (bluetoothDevice.getAddress().equals("90:59:AF:23:98:87"))
                {
                    System.out.println("I got it! sir");
                }

                if (!mLeDevices.contains(bluetoothDevice))
                {
                    mLeDevices.add(bluetoothDevice);
                    mRSSIs.add(rssi);           //新加
                    mRecords.add(scanRecord);     //新加
                    System.out.println("---------------onLeScan");
                    System.out.println(":  device.getName(): " + bluetoothDevice.getName());
                    System.out.println(":  device.getAddress(): " + bluetoothDevice.getAddress());
                    System.out.println(": bluetoothDevice.getUuids(): " + bluetoothDevice.getUuids());
                    System.out.println(":  recorde: " + bytesToHex(scanRecord));
                    int positon = mLeDevices.size();
                    //显示插入动画
                    mAdapter.notifyItemInserted(positon);
                    //通知控件更新数据
                    mAdapter.notifyItemRangeChanged(positon, mLeDevices.size() - positon);
                }
            }

            @Override
            public void onBatchScanResults(List<ScanResult> results)
            {
                super.onBatchScanResults(results);
            }

            @Override
            public void onScanFailed(int errorCode)
            {
                super.onScanFailed(errorCode);
            }

        };
    }

    private void initView()
    {
        mTb = (Toolbar) findViewById(R.id.tb);
        setSupportActionBar(mTb);

        //第一个参数是保存在哪一个xml文件中，第二个参数是操作模式，一般选择 MODE_PRIVATE
        mSharedPreferences = getSharedPreferences(Constants.getSPFILENAME(), Context.MODE_PRIVATE);

        //获取ActionBar对象
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null)
        {
            //使Actionbar的HomeAsUp按键显示出来，默认不显示，默认作用是是返回上一个活动，默认图标是一个箭头
            actionBar.setDisplayHomeAsUpEnabled(true);
        }


        initRecyclerView();
        initSwipeRefresh();
    }


    private void initRecyclerView()
    {
        //下拉刷新动作的监听初始化
        mRefreshListener = new SwipeRefreshLayout.OnRefreshListener()
        {
            @Override
            public void onRefresh()
            {
                //TODO 在内部进行刷新时间，比如加载图片，当刷新事件执行完成后使用    mSwipeRefreshLayout.setRefreshing(false);  来消除动画
                doRefresh();
            }

            //刷新需要执行的逻辑
            private void doRefresh()
            {
                new Thread(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        //进行UI更新
                        runOnUiThread(new Runnable()
                        {
                            @Override
                            public void run()
                            {
                                System.out.println("开始扫描");
                                ScanStart(); //在ScanStart的内部计时扫描，扫描结束后进行结束刷新工作
                            }
                        });
                    }
                }).start();
            }
        };

        //获取recyclerView对象
        mRv = (RecyclerView) findViewById(R.id.Rv);
        //获取一个LinearLayoutManager对象，作为mRv.setLayoutManager(manager);的参数，给mRv用
        LinearLayoutManager manager = new LinearLayoutManager(this);
        //指定方向
        manager.setOrientation(LinearLayout.VERTICAL);
        //为recycleview添加一个线性布局管理器
        mRv.setLayoutManager(manager);
        //为recycleview设间距
        mRv.addItemDecoration(new SpacesItemDecoration(4));
        //适配器，加入数据
        mAdapter = new recyclerViewAdapter(mLeDevices);
        UpdateRecyclerView();
    }

    private void initBlueTooth()
    {
        //2.1首先获取BluetoothManager
        mBluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        //2.2获取BluetoothAdapter,并打开蓝牙
        mBluetoothAdapter = mBluetoothManager.getAdapter();
        if (!mBluetoothAdapter.isEnabled())
        {
            if (!mBluetoothAdapter.enable())
                Toasty.error(getApplicationContext(), "蓝牙未打开,请手动打开后进入界面", Toast.LENGTH_SHORT);
            finish();
        }
        mBluetoothLeScanner = mBluetoothAdapter.getBluetoothLeScanner();
    }

    public void ScanStart()
    {
        if (!mBluetoothAdapter.isEnabled())  //在检测到蓝牙没有打开的时候,尝试打开蓝牙
        {
            boolean flag = mBluetoothAdapter.enable();
            System.out.println("ScanBluActivity-->" + "ScanStart" + "蓝牙打开的结果" + flag);
            if (!flag)
            {
                //打开蓝牙失败，直接提示用户，蓝牙打开失败
                Toasty.error(getApplicationContext(), "蓝牙打开失败，请手动打开蓝牙", Toast.LENGTH_SHORT).show();
                mSwipeRefreshLayout.setRefreshing(false);    //去除，停止更新逻辑
                return;
            }
            else
            {
                //打开蓝牙成功
                mBluetoothLeScanner = mBluetoothAdapter.getBluetoothLeScanner();
                if (mBluetoothLeScanner == null) //
                {
                    System.out.println("ScanBluActivity-->" + "ScanStart" + "原本蓝牙没有打开，打开蓝牙成功后，mBluetoothLeScanner获取失败");
                    Toasty.error(getApplicationContext(), "扫描失败，请重试", Toast.LENGTH_SHORT).show();
                    mSwipeRefreshLayout.setRefreshing(false);    //去除，停止更新逻辑
                    return;
                }
            }
        }
        else
        {
            //原本就打开了蓝牙
            mBluetoothLeScanner = mBluetoothAdapter.getBluetoothLeScanner();
            if (mBluetoothLeScanner == null) //
            {
                System.out.println("ScanBluActivity-->" + "ScanStart" + "原本就打开了蓝牙，mBluetoothLeScanner获取失败");
                Toasty.error(getApplicationContext(), "扫描失败，请重试", Toast.LENGTH_SHORT).show();
                mSwipeRefreshLayout.setRefreshing(false);    //去除，停止更新逻辑
                return;
            }
        }


        mLeDevices.clear();
        mRSSIs.clear();
        mRecords.clear();
        UpdateRecyclerView();


        mBluetoothLeScanner.startScan(mScanCallback);
        //开启线程，五秒后停止扫描
        new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    Thread.sleep(5000);
                    if (!mBluetoothAdapter.isEnabled())
                    {
                        runOnUiThread(new Runnable()
                        {
                            @Override
                            public void run()
                            {
                                Toasty.info(getApplicationContext(), "蓝牙已手动关闭", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                    else
                    {
                        mBluetoothLeScanner.stopScan(mScanCallback);
                        System.out.println("ScanBluActivity-->" + "run" + mScanCallback);
                    }

                    runOnUiThread(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            mSwipeRefreshLayout.setRefreshing(false);    //去除，停止更新逻辑
                        }
                    });
                }
                catch (InterruptedException e)
                {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private void initSwipeRefresh()
    {
        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.sr);
        mSwipeRefreshLayout.setColorSchemeResources(R.color.colorAccent);//为下拉刷新设置颜色
        mSwipeRefreshLayout.setOnRefreshListener(mRefreshListener);
    }

    private void UpdateRecyclerView()
    {
        //适配器，加入数据
        mAdapter = new recyclerViewAdapter(mLeDevices);
        //更新显示recyclerview数据
        mRv.setAdapter(mAdapter);
        //设置列表项的增删动画。默认动画可使用系统自带的Defa在列表元素增加或者改变的时候的动画
        mRv.setItemAnimator(new DefaultItemAnimator());
    }

    //recyclerview Adapter
    public class recyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>
    {
        //构造函数
        public recyclerViewAdapter(List<BluetoothDevice> list)
        {
            this.deviceList = list;
        }

        private List<BluetoothDevice> deviceList = new ArrayList<BluetoothDevice>();

        //定义一个内部类，继承自recyclerview，holder，来装载自己的每一个item，并在这里边为item中的内容进行赋值操作。
        // 在其构造函数中传入一个参数 itemview，这个view就是单独的每一个子视图。
        //获得了子视图，就可以根据View来获取其内部控件的id，对内部空控件进行逻辑操作了。
        class viewHolder extends RecyclerView.ViewHolder
        {
            public View aItemView;
            public TextView tv_Blu_name;
            public TextView tv_Blu_adress;
            public TextView tv_Blu_data;
            public View isChoose;

            public viewHolder(View itemView)
            {
                super(itemView);
                aItemView = itemView;
                tv_Blu_name = (TextView) itemView.findViewById(R.id.Blu_name);
                tv_Blu_adress = (TextView) itemView.findViewById(R.id.Blu_adress);
                tv_Blu_data = (TextView) itemView.findViewById(R.id.Blu_data);
                isChoose = (View) itemView.findViewById(R.id.isChoose);
            }
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
        {
            View view = null;
            RecyclerView.ViewHolder viewHolder = null;
            //初始化layooutInflate对象
            mInflat = LayoutInflater.from(ScanBluActivity.this);
            view = mInflat.inflate(R.layout.item4recylerview, parent, false);    //将自己写的布局转换为一个view对象
            //将布局对象交给 ，viewholder进行保存，布局控件填充内容等等操作
            viewHolder = new viewHolder(view);
            return viewHolder;
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, final int position)
        {
            BluetoothDevice device = deviceList.get(position);
            byte[] record = mRecords.get(position);
            viewHolder vh = (viewHolder) viewHolder;
            vh.tv_Blu_name.setText((position + 1) + ":" + device.getName());
            vh.tv_Blu_adress.setText(device.getAddress());
            vh.tv_Blu_data.setText(bytesToHex(record));

            if (deviceList.get(position).getAddress().equals(mSharedPreferences.getString(Constants.getMATCHDEVICE_MAC(), null)))
            {
                vh.isChoose.setBackgroundColor(Color.parseColor("#6495ED"));
            }

            vh.aItemView.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View view)
                {
                    showDiaglog(position);
                    mSwipeRefreshLayout.setRefreshing(false);    //停止更新逻辑,去除更新动画
                }
            });
        }

        @Override
        public int getItemCount()
        {
            return deviceList.size();
        }
    }

    //重写的间距类
    public class SpacesItemDecoration extends RecyclerView.ItemDecoration
    {
        private int space;

        public SpacesItemDecoration(int space)
        {
            this.space = space;
        }

        @Override
        public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state)
        {
            outRect.left = space;
            outRect.right = space;
            outRect.bottom = space;
            outRect.top = space;
        }
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

    void saveMAC(String address, String name)
    {
        //获取针对 mSharedPreferences的这一个特定的对象的编辑对象
        SharedPreferences.Editor spEditor = mSharedPreferences.edit();
        spEditor.putString(Constants.getMATCHDEVICE_MAC(), address);
        if (name == null)
        {
            spEditor.putString("NAME", "未命名设备");
        }
        else
        {
            spEditor.putString(Constants.getMATCHDEVICE_NAME(), name);
        }
        spEditor.commit(); //放入字符串后提交一下放入就存放进去了
    }

    private void showDiaglog(final int potion)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(ScanBluActivity.this);  //先得到构造器
        builder.setTitle(mLeDevices.get(potion).getName()); //设置标题
        builder.setMessage("这是您想要绑定的蓝牙设备吗？"); //设置内容
        builder.setIcon(R.drawable.ic_launcher);//设置图标，图片id即可
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener()
        { //设置确定按钮
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                saveMAC(mLeDevices.get(potion).getAddress(), mLeDevices.get(potion).getName());
                dialog.dismiss(); //关闭dialog
                Snackbar.make(mRv, "设备" + mLeDevices.get(potion).getName() + "已保存", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });


        builder.setNegativeButton("取消", new DialogInterface.OnClickListener()
        { //设置取消按钮
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                dialog.dismiss();
            }
        });

        builder.setNeutralButton("忽略", new DialogInterface.OnClickListener()
        {//设置忽略按钮
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                dialog.dismiss();
            }
        });
//        参数都设置完成了，创建并显示出来
        builder.create().show();
    }


}
