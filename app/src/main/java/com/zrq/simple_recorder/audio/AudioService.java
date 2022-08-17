package com.zrq.simple_recorder.audio;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.widget.RemoteViews;

import androidx.annotation.NonNull;

import com.zrq.simple_recorder.R;
import com.zrq.simple_recorder.bean.AudioBean;
import com.zrq.simple_recorder.util.Contants;

import java.io.IOException;
import java.util.List;

public class AudioService extends Service implements MediaPlayer.OnCompletionListener {

    private MediaPlayer mediaPlayer = null;
    private List<AudioBean> mList;
    private int playPosition = -1;  //记录当前播放的位置
    private RemoteViews remoteView;
    private NotificationManager manager;
    private AudioReceiver receiver;
    private final int NOTIFY_ID_MUSIC = 100;
    private Notification notification;

    /**
     * 接收通知发出的广播的action
     */
    private final String PRE_ACTION_LAST = "com.zrq.last";
    private final String PRE_ACTION_PLAY = "com.zrq.play";
    private final String PRE_ACTION_NEXT = "com.zrq.next";
    private final String PRE_ACTION_CLOSE = "com.zrq.close";

    //创建通知对象和远程View对象

    @Override
    public void onCreate() {
        super.onCreate();
        initRegisterReceiver();
        initRemoteView();
        initNotification();
    }


    /**
     * 创建广播接收者
     */
    class AudioReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            notifyUIControl(action);
        }
    }

    private void notifyUIControl(String action) {
        switch (action) {
            case PRE_ACTION_LAST:

                break;
            case PRE_ACTION_NEXT:
                break;
            case PRE_ACTION_PLAY:
                break;
            case PRE_ACTION_CLOSE:
                break;
        }
    }

    /**
     * 注册广播接收者，用于接收用户点击通知栏按钮发出的信息
     */
    private void initRegisterReceiver() {
        receiver = new AudioReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(PRE_ACTION_LAST);
        filter.addAction(PRE_ACTION_PLAY);
        filter.addAction(PRE_ACTION_NEXT);
        filter.addAction(PRE_ACTION_CLOSE);
        registerReceiver(receiver, filter);
    }

    @SuppressLint("UnspecifiedImmutableFlag")
    /**
     * 设置通知栏显示效果以及图片的点击事件
     */
    private void initRemoteView() {
        remoteView = new RemoteViews(getPackageName(), R.layout.notify_audio);

        PendingIntent lastPI = PendingIntent.getBroadcast(this, 1, new Intent(PRE_ACTION_LAST), PendingIntent.FLAG_UPDATE_CURRENT);
        remoteView.setOnClickPendingIntent(R.id.img_last, lastPI);
        PendingIntent nextPI = PendingIntent.getBroadcast(this, 1, new Intent(PRE_ACTION_NEXT), PendingIntent.FLAG_UPDATE_CURRENT);
        remoteView.setOnClickPendingIntent(R.id.img_last, nextPI);
        PendingIntent playPI = PendingIntent.getBroadcast(this, 1, new Intent(PRE_ACTION_PLAY), PendingIntent.FLAG_UPDATE_CURRENT);
        remoteView.setOnClickPendingIntent(R.id.img_last, playPI);
        PendingIntent closePI = PendingIntent.getBroadcast(this, 1, new Intent(PRE_ACTION_CLOSE), PendingIntent.FLAG_UPDATE_CURRENT);
        remoteView.setOnClickPendingIntent(R.id.img_last, closePI);
    }

    /**
     * 初始化通知栏
     */
    private void initNotification() {
        manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        Notification.Builder builder = new Notification.Builder(this);
        builder.setSmallIcon(R.mipmap.icon_app_logo)
                .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.icon_app_logo))
                .setContent(remoteView)
                .setAutoCancel(false)
                .setOngoing(true)
                .setCategory(Notification.CATEGORY_SERVICE)
                .setPriority(Notification.PRIORITY_HIGH);
        notification = builder.build();
    }

    /**
     * 更新通知栏信息的函数
     */
    private void updateNotification(int position) {
        if (mediaPlayer.isPlaying()) {
            remoteView.setImageViewResource(R.id.img_play, R.mipmap.red_pause);
        } else {
            remoteView.setImageViewResource(R.id.img_play, R.mipmap.red_play);
        }
        remoteView.setTextViewText(R.id.tv_title, mList.get(position).getTitle());
        remoteView.setTextViewText(R.id.tv_title, mList.get(position).getTitle());
        //发送通知
        manager.notify(NOTIFY_ID_MUSIC, notification);
    }

    public interface OnPlayChangeListener {

        void playChange(int changePos);
    }

    private OnPlayChangeListener onPlayChangeListener;

    public void setOnPlayChangeListener(OnPlayChangeListener onPlayChangeListener) {
        this.onPlayChangeListener = onPlayChangeListener;
    }

    /**
     * 多媒体变化时提示Activity刷新UI
     */
    public void notifyActivityRefreshUI() {
        if (onPlayChangeListener != null) {
            onPlayChangeListener.playChange(playPosition);
        }
    }

    public AudioService() {
    }

    @Override
    public void onCompletion(MediaPlayer mediaPlayer) {

    }

    public class AudioBinder extends Binder {
        public AudioService getService() {
            return AudioService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return new AudioBinder();
    }

    /**
     * 判断播放按钮有两种可能性
     * 1.不是当前播放位置被点击：进行切歌操作
     * 2.当前播放位置被点击：进行暂停/继续操作
     */
    public void cutOrPause(int clickPos) {
        int playPosition = this.playPosition;
        if (clickPos != playPosition) {

            if (playPosition != -1) {
                mList.get(playPosition).setPlaying(false);
            }
            play(clickPos);
            return;
        }
        pauseOrContinueMusic();
    }

    /**
     * 关闭通知栏，停止音乐播放
     */
    private void closeNotification() {
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
            mList.get(playPosition).setPlaying(false);
        }
        notifyActivityRefreshUI();
        manager.cancel(NOTIFY_ID_MUSIC);
    }

    /**
     * 停止音乐
     */
    public void closeMusic(){
        if (mediaPlayer != null) {
            setFlagControlThread(false);
            closeNotification();
            mediaPlayer.stop();
            playPosition = -1;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (receiver != null) {
            unregisterReceiver(receiver);
        }
        closeMusic();
    }

    /**
     * 播放音乐，点击切歌
     */
    public void play(int position) {
        if (mediaPlayer == null) {
            mediaPlayer = new MediaPlayer();
            //设置监听事件
            mediaPlayer.setOnCompletionListener(this);
        }
        //播放时获取当前歌曲列表，判断是否有歌
        mList = Contants.getsAudioList();
        if (mList.size() <= 0) {
            return;
        }
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.stop();
        }
        //切歌之前先重置，释放掉原来的资源
        try {
            mediaPlayer.reset();
            //设置播放路径
            playPosition = position;
            mediaPlayer.setDataSource(mList.get(position).getPath());
            mediaPlayer.prepare();  //同步准备
            mediaPlayer.start();
            mList.get(position).setPlaying(true);
            notifyActivityRefreshUI();
            setFlagControlThread(true);
            updateProgress();
            updateNotification(position);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 暂停/继续播放
     */
    public void pauseOrContinueMusic() {
        int playPosition = this.playPosition;
        AudioBean audio = mList.get(playPosition);
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
            audio.setPlaying(false);
        } else {
            mediaPlayer.start();
            audio.setPlaying(true);
        }
        notifyActivityRefreshUI();
        updateNotification(playPosition);
    }

    /**
     * 更新播放进度
     */
    private boolean flag = true;

    private final int PROGRESS_ID = 1;
    private final int INTERMINATE_TIME = 1000;

    public void setFlagControlThread(boolean flag) {
        this.flag = flag;
    }

    public void updateProgress() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (flag) {
                    long total = mList.get(playPosition).getDurationLong();
                    int currentPosition = mediaPlayer.getCurrentPosition();
                    //计算当前播放进度
                    int progress = (int) (currentPosition * 100 / total);
                    mList.get(playPosition).setCurrentProgress(progress);
                    handler.sendEmptyMessageDelayed(PROGRESS_ID, INTERMINATE_TIME);
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }

    Handler handler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(@NonNull Message message) {
            if (message.what == PROGRESS_ID) {
                notifyActivityRefreshUI();
            }
            return false;
        }
    });

}


