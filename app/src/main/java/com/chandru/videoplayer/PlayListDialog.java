package com.chandru.videoplayer;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.chandru.videoplayer.adapter.VideoFilesAdapter;
import com.chandru.videoplayer.models.MediaFiles;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.util.ArrayList;

public class PlayListDialog extends BottomSheetDialogFragment {

    private static final String MY_PREF ="my preference";
    ArrayList<MediaFiles> arrayList;
    VideoFilesAdapter videoFilesAdapter;
    BottomSheetDialog bottomSheetDialog;
    RecyclerView recyclerView;
    TextView folder;

    public PlayListDialog(ArrayList<MediaFiles> arrayList, VideoFilesAdapter videoFilesAdapter) {
        this.arrayList = arrayList;
        this.videoFilesAdapter = videoFilesAdapter;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        arrayList = new ArrayList<>();
        bottomSheetDialog =(BottomSheetDialog) super.onCreateDialog(savedInstanceState);

        View view = LayoutInflater.from(getContext()).inflate(R.layout.playlist_sheet_dialog,null);
        bottomSheetDialog.setContentView(view);

        recyclerView = view.findViewById(R.id.playlistRCV);
        folder = view.findViewById(R.id.playlist_name);

        SharedPreferences sharedPreferences = this.getActivity().getSharedPreferences(MY_PREF, Context.MODE_PRIVATE);
        String folderName = sharedPreferences.getString("playlistFolderName","abc");

        folder.setText(folderName);

        arrayList = fetchvideosfromfolder(folderName);
//        Toast.makeText(getContext(), folderName, Toast.LENGTH_SHORT).show();
        videoFilesAdapter = new VideoFilesAdapter(getContext(),arrayList,1);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext(),LinearLayoutManager.VERTICAL,false);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(videoFilesAdapter);
        videoFilesAdapter.notifyDataSetChanged();

        return bottomSheetDialog;
    }


    private ArrayList<MediaFiles> fetchvideosfromfolder(String folderName) {


        ArrayList<MediaFiles> videofiles = new ArrayList<>();
        Uri uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;

        String selection = MediaStore.Video.Media.DATA+" like?";
        String[] selectionArr = new String[]{"%"+folderName+"%"};
        Cursor cursor = getContext().getContentResolver().query(uri,null,selection,selectionArr,null);
//        Toast.makeText(this, cursor.toString(), Toast.LENGTH_SHORT).show();

        if (cursor != null && cursor.moveToNext()){
            do {
                @SuppressLint("Range") String id = cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media._ID));
                @SuppressLint("Range") String title = cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.TITLE));
                @SuppressLint("Range") String displayName = cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.DISPLAY_NAME));
                @SuppressLint("Range") String size = cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.SIZE));
                @SuppressLint("Range") String duration = cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.DURATION));
                @SuppressLint("Range") String path = cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.DATA));
                @SuppressLint("Range") String dateAdded = cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.DATE_ADDED));

                MediaFiles mediaFiles = new MediaFiles(id,title,displayName,size,duration,path,dateAdded);
//                Toast.makeText(this, mediaFiles.toString(), Toast.LENGTH_SHORT).show();
                videofiles.add(mediaFiles);

            }while (cursor.moveToNext());
        }
        return videofiles;

    }

}
