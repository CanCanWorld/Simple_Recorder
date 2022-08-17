package com.zrq.simple_recorder.recoder;

import android.app.Service;
import android.content.Intent;
import android.media.MediaRecorder;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;

import androidx.annotation.NonNull;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class RecorderService extends Service {

    private MediaRecorder recorder;
    private boolean isAlive = false;
    private String recorderDirPath;
    private SimpleDateFormat sdf;
    private SimpleDateFormat calSdf;
    private int time;

    public RecorderService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        sdf = new SimpleDateFormat("yyyyMMdd_HHmmss");
        calSdf = new SimpleDateFormat("HH:mm:ss");
        recorderDirPath = "/storage/emulated/0/新文件夹";
    }

    /**
     * 设置更新Activity的UI界面的回调接口
     */
    public interface OnRefreshUIThreadListener {
        void onRefresh(int fenbei, String time);
    }

    private OnRefreshUIThreadListener onRefreshUIThreadListener;

    public void setOnRefreshUIThreadListener(OnRefreshUIThreadListener onRefreshUIThreadListener) {
        this.onRefreshUIThreadListener = onRefreshUIThreadListener;
    }

    Handler handler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(@NonNull Message message) {
            if (recorder == null) {
                return false;
            }
            double ratio = (double) recorder.getMaxAmplitude();
            double db = 0;
            if (ratio > 1) {
                db = 20 * Math.log10(ratio);
            }
            time += 1000;
            if (onRefreshUIThreadListener != null) {
                String timeStr = calcTime(time);
                onRefreshUIThreadListener.onRefresh((int) db, timeStr);
            }
            return false;
        }
    });

    /**
     * 计算时间为指定格式
     *
     * @param
     * @return
     */
    private String calcTime(int mSecond) {
        mSecond -= 8 * 60 * 60 * 1000;
        String format = calSdf.format(mSecond);
        return format;
    }

    /**
     * 开启子线程实时获取音量以及当前录制的时间
     */
    Thread thread = new Thread(new Runnable() {
        @Override
        public void run() {
            while (isAlive) {
                handler.sendEmptyMessage(0);
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    });

    /**
     * 开启录音
     *
     * @param
     * @return
     */
    public void startRecorder() {
        if (recorder == null) {
            recorder = new MediaRecorder();
        }
        isAlive = true;
        recorder.reset();
        //设置录音对象的参数
        setRecorder();
        try {
            recorder.prepare();
            recorder.start();
            thread.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 停止录制
     */
    public void stopRecorder() {
        if (recorder != null) {
            recorder.stop();
            recorder = null;
            time = 0;
            isAlive = false;
        }
    }

    /**
     * 设置录音对象的参数
     */
    private void setRecorder() {
        //设置获取麦克风的声音
        recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        //设置输出格式
        recorder.setOutputFormat(MediaRecorder.OutputFormat.AMR_WB);
        //设置编码格式
        recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_WB);
        //设置输出文件
        String time = sdf.format(new Date());
        File file = new File(recorderDirPath, time + ".amr");
        if (file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        recorder.setOutputFile(file.getAbsolutePath());
        //设置最多录制十分钟
        recorder.setMaxDuration(10 * 60 * 1000);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return new RecorderBinder();
    }

    public class RecorderBinder extends Binder {
        public RecorderService getService() {
            return RecorderService.this;
        }
    }

}