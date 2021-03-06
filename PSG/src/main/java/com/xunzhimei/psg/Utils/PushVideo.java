package com.xunzhimei.psg.Utils;

import android.content.Context;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.Gravity;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.WindowManager;

import com.baidu.recorder.api.LiveSession;
import com.baidu.recorder.api.LiveSessionSW;
import com.baidu.recorder.api.SessionStateListener;
import com.xunzhimei.psg.MyApplication;

/**
 * Author: Created by 周寒 on 2018/8/10
 * Email : vihanmy@google.com
 * Notes :用于向百度云平台推流的工具类
 */
public class PushVideo
{
    private static final String TAG = "PushVideo";
    Handler mHandler = new Handler(Looper.getMainLooper());

    //推流相关
    private LiveSession mLiveSession = null;
    private SessionStateListener mStateListener = null;
    private boolean mIsFRTM = false;
    private int mCurrentCamera = -1;

    private boolean isSessionReady = false;
    private boolean isPushing = false;

    //视频参数
    private int mVideoWidth = 1280;
    private int mVideoHeight = 720;
    private int mFrameRate = 15;
    private int mBitrate = 2048000;
    private String mStreamingUrl = "rtmp://push.yiweiiot.com/game/room1";
    //播放URL "rtmp://play.yiweiiot.com/game/room1";


    //悬浮窗相关
    WindowManager mWindowManager;
    WindowManager.LayoutParams params;
    private SurfaceView mSf;

    public PushVideo()
    {
        //TODO
        mCurrentCamera = Camera.CameraInfo.CAMERA_FACING_BACK;
        initWindow();
        initRTMPSession(mSf.getHolder());
    }

    //由于推流需要在SurfaceView的显示中获取视频流，所以，必须要用一个界面来显示视频内容，显示后再进行推流。
    private void initWindow()
    {
        //①获得WindowManager对象:
        mWindowManager = (WindowManager) MyApplication.getContext().getSystemService(Context.WINDOW_SERVICE);
        //②获得WindowManager.LayoutParams对象，为后续操作做准备
        params = new WindowManager.LayoutParams();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
        {//6.0
            //params.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        }
        else
        {
            params.type = WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;
        }
        mSf = new SurfaceView(MyApplication.getContext());
        mWindowManager = (WindowManager) MyApplication.getContext()
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
        params.width = 700;
        params.height = 900;

        params.gravity = Gravity.LEFT;
        params.x = 200;
        params.y = 10;
    }

    //初始化传流
    private void initRTMPSession(SurfaceHolder sh)
    {
        mLiveSession = new LiveSessionSW(MyApplication.getContext(), mVideoWidth, mVideoHeight, mFrameRate, mBitrate, mCurrentCamera);
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
                        System.out.println("PushVideo-->" + "onSessionPrepared:" + "   " + realWidth + "----" + realHeight);
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
                    android.util.Log.e(TAG, "Starting Streaming failed!");
                }
            }

            //传输停止
            @Override
            public void onSessionStopped(int code)
            {
                android.util.Log.i(TAG, "传输停止了");
            }

            //传输发生错误
            @Override
            public void onSessionError(int code)
            {
                switch (code)
                {
                    case SessionStateListener.ERROR_CODE_OF_OPEN_MIC_FAILED:
                        android.util.Log.e(TAG, "Error occurred while opening MIC!");
                        break;
                    case SessionStateListener.ERROR_CODE_OF_OPEN_CAMERA_FAILED:
                        android.util.Log.e(TAG, "Error occurred while opening Camera!");
                        break;
                    case SessionStateListener.ERROR_CODE_OF_PREPARE_SESSION_FAILED:
                        android.util.Log.e(TAG, "Error occurred while preparing recorder!");
                        isSessionReady = false;
                        break;
                    case SessionStateListener.ERROR_CODE_OF_CONNECT_TO_SERVER_FAILED:
                        android.util.Log.e(TAG, "Error occurred while connecting to server!");
                        android.util.Log.i(TAG, "连接推流服务器失败");
                        break;
                    case SessionStateListener.ERROR_CODE_OF_DISCONNECT_FROM_SERVER_FAILED:
                        android.util.Log.e(TAG, "Error occurred while disconnecting from server!");
                        break;
                    default:
                        android.util.Log.e(TAG, "位置错误 !" + code);
                        break;
                }
            }
        };
    }

    //开始传送视频流
    public void startPush()
    {

        if (mIsFRTM == false)
        {

            if (mLiveSession.startRtmpSession(mStreamingUrl))
            {
                mHandler.post(new Runnable()
                {
                    public void run()
                    {
                        mWindowManager.addView(mSf, params);
                    }
                });
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
    public void stopPush()
    {
        if (mIsFRTM == true)
        {
            if (mLiveSession.stopRtmpSession()) ;
            {
                mWindowManager.removeView(mSf);
                android.util.Log.i(TAG, "bt_end: 视频流传输，停止成功");
            }
            mIsFRTM = false;
        }
        else
        {
            android.util.Log.i(TAG, "bt_end: 推流已停止，请不要重复停止");
        }
    }

    //换摄像头
    public void switchCamera()
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
            Log.i(TAG, "onClickSwitchCamera: 推流没开始，你换摄像头也没用");
        }
    }
}
