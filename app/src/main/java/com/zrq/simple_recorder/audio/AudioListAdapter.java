package com.zrq.simple_recorder.audio;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.zrq.simple_recorder.R;
import com.zrq.simple_recorder.bean.AudioBean;
import com.zrq.simple_recorder.databinding.ItemAudioBinding;

import java.util.List;

public class AudioListAdapter extends BaseAdapter {

    private Context mContext;
    private List<AudioBean> audios;
    private OnItemPlayClickListener onItemPlayClickListener;

    public interface OnItemPlayClickListener {
        void onItemPlayClick(AudioListAdapter adapter, View convertView, View playView, int position);

    }

    public void setOnItemPlayClickListener(OnItemPlayClickListener onItemPlayClickListener) {
        this.onItemPlayClickListener = onItemPlayClickListener;
    }

    public AudioListAdapter(Context mContext, List<AudioBean> audios) {
        this.mContext = mContext;
        this.audios = audios;
    }

    @Override
    public int getCount() {
        return audios.size();
    }

    @Override
    public Object getItem(int i) {
        return audios.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        ViewHolder holder = null;
        if (view == null) {
            view = LayoutInflater.from(mContext).inflate(R.layout.item_audio, viewGroup, false);
            holder = new ViewHolder(view);
            view.setTag(holder);
        } else {
            holder = (ViewHolder) view.getTag();
        }
        AudioBean audio = audios.get(i);
        holder.ab.tvTitle.setText(audio.getTitle());
        holder.ab.tvTime.setText(audio.getTime());
        holder.ab.tvDuration.setText(audio.getDuration());
        if (audio.isPlaying()) {
            holder.ab.llControl.setVisibility(View.VISIBLE);
            holder.ab.pb.setMax(100);
            holder.ab.pb.setProgress(audio.getCurrentProgress());
            holder.ab.ivPlay.setImageResource(R.mipmap.red_pause);
        } else {
            holder.ab.llControl.setVisibility(View.GONE);
            holder.ab.ivPlay.setImageResource(R.mipmap.red_play);
        }
        View itemView = view;
        holder.ab.ivPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (onItemPlayClickListener != null) {
                    onItemPlayClickListener.onItemPlayClick(AudioListAdapter.this, itemView, v, i);
                }
            }
        });
        return view;
    }

    static class ViewHolder {
        ItemAudioBinding ab;

        public ViewHolder(View v) {
            ab = ItemAudioBinding.bind(v);
        }
    }
}
