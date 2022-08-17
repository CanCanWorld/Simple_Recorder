package com.zrq.simple_recorder;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.zrq.simple_recorder.audio.AudioListActivity;
import com.zrq.simple_recorder.databinding.ActivityMainBinding;
import com.zrq.simple_recorder.util.Contants;
import com.zrq.simple_recorder.util.IFileInter;
import com.zrq.simple_recorder.util.PermissionUtils;
import com.zrq.simple_recorder.util.SDCardUtils;

import java.io.File;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private TextView mTvJump;
    private ActivityMainBinding binding;
    int time = 5;   //倒计时
    String[] permissions = {Manifest.permission.RECORD_AUDIO, Manifest.permission.READ_EXTERNAL_STORAGE};
    Handler handler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(@NonNull Message message) {
            if (message.what == 1) {
                time--;
                if (time == 0) {
                    startActivity(new Intent(MainActivity.this, AudioListActivity.class));
                    finish();
                } else {
                    binding.tvJump.setText("倒计时" + time+"秒\nVIP可跳过");
                    handler.sendEmptyMessageDelayed(1, 1000);
                }
            }
            return false;
        }
    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        binding.tvJump.setText("倒计时" + time+"秒\nVIP可跳过");
        PermissionUtils.getInstance().onRequestPermission(this, permissions, listener);
        mTvJump = findViewById(R.id.tv_jump);
        mTvJump.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast toast = Toast.makeText(getApplicationContext(), "一定要充VIP才能一人攻沙虐全场！！！", Toast.LENGTH_SHORT);
                toast.show();
            }
        });
    }

    PermissionUtils.OnPermissionCallbackListener listener = new PermissionUtils.OnPermissionCallbackListener() {
        @Override
        public void onGranted() {
            //判断是否有应用文件夹，如果没有就创建应用文件夹
            createAppDir();
            //倒计时进入播放录音界面
            handler.sendEmptyMessageDelayed(1, 1000);
        }

        @Override
        public void onDenied(List<String> deniedPermissions) {
            PermissionUtils.getInstance().showDialogTipUserGotoAppSetting(MainActivity.this);
        }
    };

    private void createAppDir() {
        File recordDir = SDCardUtils.getInstance().createAppFetchDir(IFileInter.FETCH_DIR_AUDIO);
        Contants.PATH_FETCH_DIR_RECORD = recordDir.getAbsolutePath();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        PermissionUtils.getInstance().onRequestPermissionResult(this, requestCode, permissions, grantResults);
    }
}