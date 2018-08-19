package com.xunzhimei.psg.Utils;


import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Vibrator;

import com.xunzhimei.psg.MyApplication;

import static android.content.Context.VIBRATOR_SERVICE;

/**
 * Author: Created by 周寒 on 2018/8/15
 * Email : vihanmy@google.com
 * Notes :
 */
public class NotifyMyPhone
{

    private final Uri mAlert;
    private MediaPlayer mMediaPlayer;
    private Vibrator mVibrator;

    public NotifyMyPhone()
    {
        mVibrator = (Vibrator) MyApplication.getContext().getSystemService(VIBRATOR_SERVICE);
        mAlert = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);//用于获取手机默认铃声的Uri

    }

    public void nowNotifyMyPhone(final int time)
    {
        try
        {
            mMediaPlayer = new MediaPlayer();
            mMediaPlayer.setDataSource(MyApplication.getContext(), mAlert);
            mMediaPlayer.setAudioStreamType(AudioManager.STREAM_RING);//告诉mediaPlayer播放的是铃声流
            mMediaPlayer.setLooping(true);
            mMediaPlayer.prepare();
            mMediaPlayer.start();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }


        if (mVibrator.hasVibrator())//检查硬件是否有振动器
        {
            long[] interval = {0, 200, 10, 100};    //OFF/ON/OFF/ON
            mVibrator.vibrate(interval, 0); // 震动
        }

        //延时线程用于延时短暂时间后取消响铃，震动
        new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    Thread.sleep(time);
                    mVibrator.cancel();
                    if (mMediaPlayer != null)
                    {
                        if (mMediaPlayer.isPlaying())
                        {
                            mMediaPlayer.stop();
                            mMediaPlayer.release();
                        }
                    }
                }
                catch (InterruptedException e)
                {
                    e.printStackTrace();
                }
            }
        }).start();
    }

}
