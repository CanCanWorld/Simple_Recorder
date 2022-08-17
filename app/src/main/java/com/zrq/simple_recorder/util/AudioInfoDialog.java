package com.zrq.simple_recorder.util;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.Display;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;

import com.zrq.simple_recorder.bean.AudioBean;
import com.zrq.simple_recorder.databinding.DialogAudioinfoBinding;

import java.text.DecimalFormat;

public class AudioInfoDialog extends Dialog {

    private DialogAudioinfoBinding binding;

    public AudioInfoDialog(@NonNull Context context) {
        super(context);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DialogAudioinfoBinding.inflate(getLayoutInflater());
        CardView root = binding.getRoot();
        setContentView(root);
        binding.tvClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                cancel();
            }
        });
    }

    /**
     * 设置对话框宽度和屏幕宽度一致
     */
    public void setDialogWidth() {
        Window window = getWindow();
        WindowManager.LayoutParams wlp = window.getAttributes();
        Display display = window.getWindowManager().getDefaultDisplay();
        wlp.width = display.getWidth() - 30;
        wlp.gravity = Gravity.BOTTOM;
        window.setBackgroundDrawableResource(android.R.color.transparent);
        window.setAttributes(wlp);
    }

    public void setFileInfo(AudioBean audio) {
        binding.tvTitle.setText(audio.getTitle());
        binding.tvTime.setText(audio.getTime());
        binding.tvPath.setText(audio.getPath());
        String size = calcFileSize(audio.getFileLength());
        binding.tvSize.setText(size);
    }

    private String calcFileSize(long fileLength) {
        DecimalFormat format = new DecimalFormat("#.00");
        if (fileLength >= 1024 * 1024) {
            return format.format(fileLength * 1.0 / (1024 * 1024)) + "MB";
        } else if (fileLength >= 1024) {
            return format.format(fileLength * 1.0 / 1024) + "KB";
        } else if (fileLength < 1024) {
            return fileLength + "B";
        }
        return "0KB";
    }
}
