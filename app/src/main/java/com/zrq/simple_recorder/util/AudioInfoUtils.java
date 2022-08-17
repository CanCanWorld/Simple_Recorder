package com.zrq.simple_recorder.util;

import android.media.MediaMetadataRetriever;

import java.text.SimpleDateFormat;
import java.util.Date;

public class AudioInfoUtils {

    private MediaMetadataRetriever mediaMetadataRetriever;
    private static AudioInfoUtils audioInfoUtils;

    private AudioInfoUtils() {
    }

    public static AudioInfoUtils getInstance() {
        if (audioInfoUtils == null) {
            synchronized (AudioInfoUtils.class) {
                if (audioInfoUtils == null) {
                    audioInfoUtils = new AudioInfoUtils();
                }
            }
        }
        return audioInfoUtils;
    }

    public long getAudioFileDuration(String filePath) {
        long duration = 0;
        if (mediaMetadataRetriever == null) {
            mediaMetadataRetriever = new MediaMetadataRetriever();
        }
        mediaMetadataRetriever.setDataSource(filePath);
        String s = mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
        duration = Long.parseLong(s);
        return duration;
    }

    public String getAudioFileFormatDuration(String format, long durlong) {
        durlong -= 8 * 3600 * 1000;
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        return sdf.format(new Date(durlong));
    }

    public String getAudioFileFormatDuration(long durlong) {
        return getAudioFileFormatDuration("HH:mm:ss", durlong);
    }

    public String getAudioFileArtist(String filePath) {
        if (mediaMetadataRetriever == null) {
            mediaMetadataRetriever = new MediaMetadataRetriever();
        }
        mediaMetadataRetriever.setDataSource(filePath);
        String artist = mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST);
        return artist;
    }

    public void releaseRetriever() {
        if (mediaMetadataRetriever != null) {
            mediaMetadataRetriever.release();
            mediaMetadataRetriever = null;
        }
    }
}
