package com.xunzhimei.psg.Activieties;

import android.app.Activity;
import android.content.Context;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;

import com.baidu.recorder.api.LiveSession;
import com.baidu.recorder.api.LiveSessionSW;
import com.baidu.recorder.api.SessionStateListener;
import com.xunzhimei.psg.R;

public class TestActivity extends Activity
{
    private static final String TAG = "Main2Activity";

    //推流相关
    private LiveSession mLiveSession = null;
    private SessionStateListener mStateListener = null;
    private boolean mIsFRTM = false;
    private boolean isSessionReady = false;
    private int mCurrentCamera = -1;

    //视频参数
    private int mVideoWidth = 1280;
    private int mVideoHeight = 720;
    private int mFrameRate = 15;
    private int mBitrate = 2048000;
    private String mStreamingUrl = "rtmp://push.yiweiiot.com/game/room1";
    //private String mStreamingUrl = "rtmp://play.yiweiiot.com/game/room1";

    //悬浮窗相关
    WindowManager mWindowManager;
    WindowManager.LayoutParams params;
    private SurfaceView mSf;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);
        mCurrentCamera = Camera.CameraInfo.CAMERA_FACING_BACK;
        initWindow();
        initRTMPSession(mSf.getHolder());
    }


    //---------------------------------------------推流相关-------------------------------------------------
    private void initWindow()
    {
        //①获得WindowManager对象:
        mWindowManager = (WindowManager) getApplicationContext().getSystemService(Context.WINDOW_SERVICE);
        //②获得WindowManager.LayoutParams对象，为后续操作做准备
        params = new WindowManager.LayoutParams();
        mSf = new SurfaceView(getApplicationContext());
        mWindowManager = (WindowManager) getApplicationContext()
                .getSystemService(Context.WINDOW_SERVICE);
//        其实这个LayoutParams类是用于child view（子视图） 向 parent view（父视图）传达自己的意愿的一个东西
        params = new WindowManager.LayoutParams();
        // 设置窗口中添加的View的类型
        params.type = WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;
        // 设置窗口中添加的View的不可触摸
        params.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;

        // 悬浮窗不可触摸，不接受任何事件,同时不影响后面的事件响应
        params.format = PixelFormat.RGBA_8888;

        // 设置悬浮框的宽高
        params.width = 1;
        params.height = 1;

        params.gravity = Gravity.LEFT;
        params.x = 200;
        params.y = 10;
    }

    //初始化传流
    private void initRTMPSession(SurfaceHolder sh)
    {
        mLiveSession = new LiveSessionSW(this, mVideoWidth, mVideoHeight, mFrameRate, mBitrate, mCurrentCamera);
        mLiveSession.setStateListener(mStateListener);
        mLiveSession.bindPreviewDisplay(sh);
        mLiveSession.prepareSessionAsync();
    }

    //传流过程中的监听初始化
    private void initStateListener()
    {
        mStateListener = new SessionStateListener()
        {
            //传输对话已经准备好
            @Override
            public void onSessionPrepared(int code)
            {
                if (code == SessionStateListener.RESULT_CODE_OF_OPERATION_SUCCEEDED)
                {
                    isSessionReady = true;
                    int realWidth = mLiveSession.getAdaptedVideoWidth();
                    int realHeight = mLiveSession.getAdaptedVideoHeight();
                    if (realHeight != mVideoHeight || realWidth != mVideoWidth)
                    {
                        mVideoHeight = realHeight;
                        mVideoWidth = realWidth;
                    }
                }
            }

            //传输开始
            @Override
            public void onSessionStarted(int code)
            {
                if (code == SessionStateListener.RESULT_CODE_OF_OPERATION_SUCCEEDED)
                {
                    mLiveSession.enableDefaultBeautyEffect(true);
                }
                else
                {
                    Log.e(TAG, "Starting Streaming failed!");
                }
            }

            //传输停止
            @Override
            public void onSessionStopped(int code)
            {
                Log.i(TAG, "传输停止了");
            }

            //传输发生错误
            @Override
            public void onSessionError(int code)
            {
                switch (code)
                {
                    case SessionStateListener.ERROR_CODE_OF_OPEN_MIC_FAILED:
                        Log.e(TAG, "Error occurred while opening MIC!");
                        break;
                    case SessionStateListener.ERROR_CODE_OF_OPEN_CAMERA_FAILED:
                        Log.e(TAG, "Error occurred while opening Camera!");
                        break;
                    case SessionStateListener.ERROR_CODE_OF_PREPARE_SESSION_FAILED:
                        Log.e(TAG, "Error occurred while preparing recorder!");
                        isSessionReady = false;
                        break;
                    case SessionStateListener.ERROR_CODE_OF_CONNECT_TO_SERVER_FAILED:
                        Log.e(TAG, "Error occurred while connecting to server!");
                        Log.i(TAG, "连接推流服务器失败");
                        break;
                    case SessionStateListener.ERROR_CODE_OF_DISCONNECT_FROM_SERVER_FAILED:
                        Log.e(TAG, "Error occurred while disconnecting from server!");
                        break;
                    default:
                        Log.e(TAG, "位置错误 !" + code);
                        break;
                }
            }
        };
    }

    //开始传送视频流
    public void bt_start(View v)
    {

        if (mIsFRTM == false)
        {

            if (mLiveSession.startRtmpSession(mStreamingUrl))
            {
                mWindowManager.addView(mSf, params);
                Log.i(TAG, "bt_start: 开始传输视频流");
            }
            mIsFRTM = true;
        }
        else
        {
            Log.i(TAG, "bt_start: 推流正在进行请不要重复开始推流");
        }

    }

    //停止传送视频流
    public void bt_end(View v)
    {
        if (mIsFRTM == true)
        {
            if (mLiveSession.stopRtmpSession()) ;
            {

                mWindowManager.removeView(mSf);
                Log.i(TAG, "bt_end: 视频流传输，停止成功");
            }
            mIsFRTM = false;
        }
        else
        {
            Log.i(TAG, "bt_end: 推流已停止，请不要重复停止");
        }
    }

    //换摄像头
    public void onClickSwitchCamera(View v)
    {
        if (mIsFRTM)
        {
            if (mLiveSession.canSwitchCamera())
            {
                if (mCurrentCamera == Camera.CameraInfo.CAMERA_FACING_BACK)
                {
                    mCurrentCamera = Camera.CameraInfo.CAMERA_FACING_FRONT;
                    mLiveSession.switchCamera(mCurrentCamera);
                    Log.i(TAG, "onClickSwitchCamera: 正在使用前置摄像头");
                }
                else
                {
                    mCurrentCamera = Camera.CameraInfo.CAMERA_FACING_BACK;
                    mLiveSession.switchCamera(mCurrentCamera);
                    Log.i(TAG, "onClickSwitchCamera: 正在使用后置摄像头");
                }
            }
            else
            {
                Log.i(TAG, "onClickSwitchCamera: 抱歉！该分辨率下不支持切换摄像头");
            }
        }
        else
        {
            Log.i(TAG, "onClickSwitchCamera: 推流没开始，你换摄像头没用");
        }
    }


}