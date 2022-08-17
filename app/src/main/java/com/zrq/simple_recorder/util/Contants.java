package com.zrq.simple_recorder.util;

import com.zrq.simple_recorder.bean.AudioBean;

import java.util.List;

public class Contants {
    public static String PATH_APP_DIR;
    public static String PATH_FETCH_DIR_RECORD;

    private static List<AudioBean> sAudioList;

    public static void setsAudioList(List<AudioBean> sAudioList){
        if (sAudioList != null) {
            Contants.sAudioList = sAudioList;
        }
    }
    public static List<AudioBean> getsAudioList() {
        return sAudioList;
    }
}
