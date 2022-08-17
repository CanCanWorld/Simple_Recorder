package com.zrq.simple_recorder.recoder;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageView;

import com.zrq.simple_recorder.R;
import com.zrq.simple_recorder.audio.AudioListActivity;
import com.zrq.simple_recorder.databinding.ActivityRecoderBinding;
import com.zrq.simple_recorder.util.StartSystemPageUtils;

public class RecorderActivity extends AppCompatActivity implements View.OnClickListener {

    private ImageView mIvBack;
    private ImageView mIvStop;
    private ActivityRecoderBinding binding;
    private RecorderService recorderService;
    RecorderService.OnRefreshUIThreadListener refreshUIThreadListener = new RecorderService.OnRefreshUIThreadListener() {
        @Override
        public void onRefresh(int fenbei, String time) {
            binding.voicLine.setVolume(fenbei);
            binding.tvDuration.setText(time);
        }
    };

    ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            RecorderService.RecorderBinder binder = (RecorderService.RecorderBinder) iBinder;
            recorderService = binder.getService();
            recorderService.startRecorder();
            recorderService.setOnRefreshUIThreadListener(refreshUIThreadListener);
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRecoderBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        Intent intent = new Intent(this, RecorderService.class);
        bindService(intent, connection, BIND_AUTO_CREATE);
        mIvBack = findViewById(R.id.img_back);
        mIvStop = findViewById(R.id.iv_stop);
        mIvBack.setOnClickListener(this);
        mIvStop.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.img_back:
                StartSystemPageUtils.goToAppHome(this);
                break;
            case R.id.iv_stop:
                recorderService.stopRecorder();
                Intent intent = new Intent(this, AudioListActivity.class);
                startActivity(intent);
                finish();
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (connection != null) {
            unbindService(connection);
        }
    }

    public boolean onKeyDown(int keyCode, KeyEvent event){
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            StartSystemPageUtils.goToAppHome(this);
            return true;
        }
        return  super.onKeyDown(keyCode, event);
    }

}




