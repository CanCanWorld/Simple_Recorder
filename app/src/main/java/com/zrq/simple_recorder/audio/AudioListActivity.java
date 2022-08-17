package com.zrq.simple_recorder.audio;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.PopupMenu;

import com.zrq.simple_recorder.R;
import com.zrq.simple_recorder.bean.AudioBean;
import com.zrq.simple_recorder.databinding.ActivityAudioListBinding;
import com.zrq.simple_recorder.recoder.RecorderActivity;
import com.zrq.simple_recorder.util.AudioInfoDialog;
import com.zrq.simple_recorder.util.AudioInfoUtils;
import com.zrq.simple_recorder.util.Contants;
import com.zrq.simple_recorder.util.DialogUtils;
import com.zrq.simple_recorder.util.RenameDialog;

import java.io.File;
import java.io.FilenameFilter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class AudioListActivity extends AppCompatActivity {

    private static final String TAG = "AudioListActivity";
    private ActivityAudioListBinding binding;
    private List<AudioBean> audios;
    private AudioListAdapter adapter;
    private AudioService audioService;

    ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            AudioService.AudioBinder audioBinder = (AudioService.AudioBinder) iBinder;
            audioService = audioBinder.getService();
            audioService.setOnPlayChangeListener(playChangeListener);

        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {

        }
    };

    AudioService.OnPlayChangeListener playChangeListener = new AudioService.OnPlayChangeListener() {
        @Override
        public void playChange(int changePos) {
            adapter.notifyDataSetChanged();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAudioListBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        //绑定服务
        Intent intent = new Intent(this, AudioService.class);
        bindService(intent, connection, BIND_AUTO_CREATE);

        audios = new ArrayList<>();
        adapter = new AudioListAdapter(this, audios);
        binding.lvAudio.setAdapter(adapter);
        //将音频对象集合保存到全局变量中
        Contants.setsAudioList(audios);
        //加载数据
        loadDatas();
        setEvents();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(connection);
    }

    private void setEvents() {
        adapter.setOnItemPlayClickListener(playClickListener);
        binding.lvAudio.setOnItemLongClickListener(longClickListener);
        binding.ibAudio.setOnClickListener(onClickListener);
    }

    View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            audioService.closeMusic();
            startActivity(new Intent(AudioListActivity.this, RecorderActivity.class));
            finish();
        }
    };

    //点击每一个播放按钮会回调的方法
    AudioListAdapter.OnItemPlayClickListener playClickListener = new AudioListAdapter.OnItemPlayClickListener() {
        @Override
        public void onItemPlayClick(AudioListAdapter adapter, View convertView, View playView, int position) {
            for (int i = 0; i < audios.size(); i++) {
                if (i == position) {
                    continue;
                }
                AudioBean audio = audios.get(i);
                audio.setPlaying(false);
            }
            //获取当前条目的播放状态
            boolean isPlaying = audios.get(position).isPlaying();
            audios.get(position).setPlaying(!isPlaying);
            adapter.notifyDataSetChanged();
            audioService.cutOrPause(position);
        }
    };

    AdapterView.OnItemLongClickListener longClickListener = new AdapterView.OnItemLongClickListener() {
        @Override
        public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
            showPopMenu(view, i);
            return false;
        }
    };

    /**
     * 长按弹出menu窗口
     *
     * @param view
     * @param i
     */
    private void showPopMenu(View view, int i) {
        PopupMenu popupMenu = new PopupMenu(this, view, Gravity.RIGHT);
        MenuInflater menuInflater = popupMenu.getMenuInflater();
        menuInflater.inflate(R.menu.audio_menu, popupMenu.getMenu());
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                switch (menuItem.getItemId()) {
                    case R.id.menu_info:
                        showFileInfoDialog(i);
                        break;
                    case R.id.menu_del:
                        deleteFileByPos(i);
                        break;
                    case R.id.menu_rename:
                        Log.e(TAG, "onMenuItemClick: ");
                        showRenameDialog(i);
                        break;
                }
                return false;
            }
        });
        popupMenu.show();
    }

    /**
     * 显示文件详情的对话框
     * @param i
     */
    private void showFileInfoDialog(int i) {
        AudioBean audio = audios.get(i);
        AudioInfoDialog dialog = new AudioInfoDialog(this);
        dialog.show();
        dialog.setFileInfo(audio);
        dialog.setDialogWidth();
        dialog.setCanceledOnTouchOutside(false);
    }

    /**
     * 重命名操作
     *
     * @param i
     */
    private void showRenameDialog(int i) {
        AudioBean audio = audios.get(i);
        String title = audio.getTitle();
        RenameDialog dialog = new RenameDialog(this);
        dialog.show();
        dialog.setDialogWidth();
        dialog.setTipText(title);
        dialog.setOnEnsureListener(new RenameDialog.OnEnsureListener() {
            @Override
            public void onEnsure(String msg) {
                renameByPosition(msg, i);
            }
        });
    }

    /**
     * 对于指定位置的文件重命名
     *
     * @param msg
     */
    private void renameByPosition(String msg, int i) {
        AudioBean audio = audios.get(i);
        if (audio.getTitle().equals(msg)) {
            return;
        }
        String path = audio.getPath();
        String fileSuffix = audio.getFileSuffix();
        File srcFile = new File(path);
        String destPath = srcFile.getParent() + File.separator + msg + fileSuffix;
        File destFile = new File(destPath);
        //物理重命名操作
        Log.e(TAG, "renameByPosition: " + destPath);
        srcFile.renameTo(destFile);
        //内存中修改
        audio.setTitle(msg);
        audio.setPath(destPath);
        adapter.notifyDataSetChanged();
    }

    /**
     * 删除指定位置的文件
     *
     * @param i
     */
    private void deleteFileByPos(int i) {
        AudioBean audio = audios.get(i);
        String title = audio.getTitle();
        String path = audio.getPath();
        DialogUtils.showNormalDialog(this, "提示信息", "真的要抛弃我吗T^T", "确定", new DialogUtils.OnLeftClickListener() {
            @Override
            public void onLeftClick() {
                File file = new File(path);
                file.getAbsoluteFile().delete();    //物理删除
                audios.remove(audio);
                adapter.notifyDataSetChanged();
            }
        }, "不要走", null);
    }


    private void loadDatas() {
        File fetchFile = new File("/storage/emulated/0/新文件夹");
        Log.d("loadDatas", fetchFile.getAbsolutePath());
        File[] files = fetchFile.listFiles();
        for (File file : files) {
            Log.d("loadDatas", file.getName());
        }
        File[] listFiles = fetchFile.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File file, String s) {
                if (new File(file, s).isDirectory()) {

                    return false;
                }
                if (s.endsWith(".mp3") || s.endsWith(".amr")) {
                    return true;

                }
                return false;
            }
        });

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");
        AudioInfoUtils audioInfoUtils = AudioInfoUtils.getInstance();

        for (int i = 0; i < listFiles.length; i++) {
            File audioFile = listFiles[i];
            String fname = audioFile.getName();
            String title = fname.substring(0, fname.lastIndexOf("."));
            String suffix = fname.substring(fname.lastIndexOf("."));
            long flastMod = audioFile.lastModified();
            String time = sdf.format(flastMod);
            long flength = audioFile.length();
            String audioPath = audioFile.getAbsolutePath();
            long duration = audioInfoUtils.getAudioFileDuration(audioPath);
            String formatDuration = audioInfoUtils.getAudioFileFormatDuration(duration);
            AudioBean audio = new AudioBean(i + "", title, time, formatDuration, audioPath,
                    duration, flastMod, suffix, flength);
            audios.add(audio);
        }
        audioInfoUtils.releaseRetriever();
        Collections.sort(audios, new Comparator<AudioBean>() {
            @Override
            public int compare(AudioBean audioBean, AudioBean t1) {
                if (audioBean.getLastModified() < t1.getLastModified()) {
                    return 1;
                } else if (audioBean.getLastModified() == t1.getLastModified()) {
                    return 0;
                }
                return -1;
            }
        });
        adapter.notifyDataSetChanged();
    }
}