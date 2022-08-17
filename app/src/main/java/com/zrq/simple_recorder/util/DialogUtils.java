package com.zrq.simple_recorder.util;

import android.content.Context;
import android.content.DialogInterface;

import androidx.appcompat.app.AlertDialog;

public class DialogUtils {

    public interface OnLeftClickListener{
        void onLeftClick();
    }
    public interface OnRightClickListener{
        void onRightClick();
    }
    public static void showNormalDialog(Context context, String title, String msg, String leftBtn
            , OnLeftClickListener leftListener, String rightBtn, OnRightClickListener rightListener){
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(title).setMessage(msg);
        builder.setNegativeButton(leftBtn, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if (leftListener != null) {
                    leftListener.onLeftClick();
                    dialogInterface.cancel();
                }
            }
        });
        builder.setPositiveButton(rightBtn, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if (rightListener != null) {
                    rightListener.onRightClick();
                    dialogInterface.cancel();
                }
            }
        });
        builder.create().show();
    }
}
