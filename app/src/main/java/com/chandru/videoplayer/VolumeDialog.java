package com.chandru.videoplayer;

import android.app.Dialog;
import android.content.Context;
import android.graphics.PorterDuff;
import android.media.AudioManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDialogFragment;

public class VolumeDialog extends AppCompatDialogFragment {
    private static final String TAG = "VolumeDialog";

    private TextView volume_no;
    private ImageView close;
    private SeekBar seekBar;
    AudioManager audioManager;

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.vol_dialog_item,null);
        builder.setView(view);
        getActivity().setVolumeControlStream(AudioManager.STREAM_MUSIC);

        close = view.findViewById(R.id.vol_cls);
        volume_no = view.findViewById(R.id.vol_number);
        seekBar = view.findViewById(R.id.vol_seek);
        seekBar.getProgressDrawable().setColorFilter(getResources().getColor(android.R.color.holo_purple), PorterDuff.Mode.MULTIPLY);
        seekBar.getThumb().setColorFilter(getResources().getColor(android.R.color.holo_orange_dark),PorterDuff.Mode.SRC_IN);

        audioManager = (AudioManager)getContext().getSystemService(Context.AUDIO_SERVICE);
        seekBar.setMax(audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC));
        seekBar.setProgress(audioManager.getStreamVolume(AudioManager.STREAM_MUSIC));


        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                audioManager.setStreamVolume(AudioManager.STREAM_MUSIC,progress,0);
                int mediavolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
                Log.d(TAG,"vol"+mediavolume);
                int maxvol = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
                Log.d(TAG,"maxvol"+maxvol);
                double volPer = Math.ceil((((double) mediavolume/(double)maxvol)*(double)100));
                volume_no.setText("" +volPer);

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
        return builder.create();
    }
}
