package com.zrq.simple_recorder.util;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Display;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;

import androidx.annotation.NonNull;

import com.zrq.simple_recorder.R;
import com.zrq.simple_recorder.databinding.DialogRenameBinding;

public class RenameDialog extends Dialog implements View.OnClickListener {

    private DialogRenameBinding binding;

    //创建点击确定执行的接口函数
    public interface OnEnsureListener {
        void onEnsure(String msg);
    }

    private OnEnsureListener onEnsureListener;

    public void setOnEnsureListener(OnEnsureListener onEnsureListener) {
        this.onEnsureListener = onEnsureListener;
    }

    public RenameDialog(@NonNull Context context) {
        super(context);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DialogRenameBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        binding.btnEnsure.setOnClickListener(this);
        binding.btnCancel.setOnClickListener(this);
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_ensure:
                if (onEnsureListener != null) {
                    String msg = binding.etRename.getText().toString().trim();
                    onEnsureListener.onEnsure(msg);
                }
                cancel();
                break;
            case R.id.btn_cancel:
                cancel();
                break;
        }
    }

    /**
     * 设置EditText显示原来的标题名称
     */
    public void setTipText(String oldText) {
        binding.etRename.setText(oldText);
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
        handler.sendEmptyMessageDelayed(1, 100);
    }
    Handler handler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(@NonNull Message message) {
            InputMethodManager manager = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            manager.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
            return false;
        }
    });
}
