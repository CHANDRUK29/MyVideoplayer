package com.chandru.videoplayer.adapter;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Bundle;
import android.os.SystemClock;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.chandru.videoplayer.R;
import com.chandru.videoplayer.VideoPlayActivity;
import com.chandru.videoplayer.models.MediaFiles;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.io.File;
import java.util.ArrayList;

public class VideoFilesAdapter extends RecyclerView.Adapter<VideoFilesAdapter.VideoViewHolder>{
    public Context context;
    public ArrayList<MediaFiles> videolist;
    BottomSheetDialog bottomSheetDialog;
    private int viewType;

    public VideoFilesAdapter(Context context, ArrayList<MediaFiles> videolist,int viewType) {
        this.context = context;
        this.videolist = videolist;
        this.viewType = viewType;
    }

    @NonNull
    @Override
    public VideoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.video_item,parent,false);
        return new VideoViewHolder(view);
        /// another method:
//        VideoViewHolder viewHolder = new VideoViewHolder(view);
//        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull VideoViewHolder holder, @SuppressLint("RecyclerView") int position) {

        holder.videoName.setText(videolist.get(position).getDiplayName());

        String size = videolist.get(position).getSize();
        holder.videoSize.setText(android.text.format.Formatter.formatFileSize(context,Long.parseLong(size)));

        double millisecs = Double.parseDouble(videolist.get(position).getDuration());
        holder.videoDuration.setText(timeconversion((long) millisecs));

        Glide.with(context).load(new File(videolist.get(position).getPath())).into(holder.thumbnail);

        if (viewType == 0) {
            holder.moreoption.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    bottomSheetDialog = new BottomSheetDialog(context, R.style.BottomSheetTheme);
                    View sheetview = LayoutInflater.from(context).inflate(R.layout.video_bottomsheet_layout, v.findViewById(R.id.bottom_sheet));
                    //play bottom sheet
                    sheetview.findViewById(R.id.bs_play).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            holder.itemView.performClick();
                            bottomSheetDialog.dismiss();
                        }
                    });
                    //Rename bottom Sheet
                    sheetview.findViewById(R.id.bs_rename).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            AlertDialog.Builder alertDialog = new AlertDialog.Builder(context);
                            alertDialog.setTitle("Rename to");
                            EditText editText = new EditText(context);
                            String path = videolist.get(position).getPath();
                            final File file = new File(path);
                            String videoName = file.getName();
                            ///abc.mp4
                            videoName = videoName.substring(0, videoName.lastIndexOf("."));
                            editText.setText(videoName);
                            alertDialog.setView(editText);
                            editText.requestFocus();

                            alertDialog.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    if (TextUtils.isEmpty(editText.getText().toString())) {
                                        Toast.makeText(context, "Can't Rename a Empty File!!", Toast.LENGTH_SHORT).show();
                                        return;
                                    }
                                    String onlypath = file.getParentFile().getAbsolutePath();
                                    String ext = file.getAbsolutePath();
                                    ext = ext.substring(ext.lastIndexOf("."));
                                    ///Media/video/lockvideo.mp4
                                    String newpath = onlypath + "/" + editText.getText().toString() + ext;
                                    File newFile = new File(newpath);
                                    boolean rename = file.renameTo(newFile);
                                    if (rename) {
                                        ContentResolver contentResolver = context.getApplicationContext().getContentResolver();
                                        contentResolver.delete(MediaStore.Files.getContentUri("external"),
                                                MediaStore.MediaColumns.DATA + "=?", new String[]{file.getAbsolutePath()});
                                        Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                                        intent.setData(Uri.fromFile(newFile));
                                        context.getApplicationContext().sendBroadcast(intent);

                                        notifyDataSetChanged();
                                        Toast.makeText(context, "Video Named", Toast.LENGTH_SHORT).show();

                                        SystemClock.sleep(200);
                                        ((Activity) context).recreate();
                                    } else {
                                        Toast.makeText(context, "Process Failed", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                            alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            });
                            alertDialog.create().show();
                            bottomSheetDialog.dismiss();
                        }
                    });
                    //Share bottom Sheet
                    sheetview.findViewById(R.id.bs_share).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Uri u = Uri.parse(videolist.get(position).getPath());
                            Intent shareIntent = new Intent(Intent.ACTION_SEND);
                            shareIntent.setType("video/*");
                            shareIntent.putExtra(Intent.EXTRA_STREAM, u);
                            context.startActivity(Intent.createChooser(shareIntent, "Share Video Via"));
                            bottomSheetDialog.dismiss();

                        }
                    });
                    //Delete bottom Sheet
                    sheetview.findViewById(R.id.bs_delete).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            AlertDialog.Builder dialog = new AlertDialog.Builder(context);
                            dialog.setTitle("Delete");
                            dialog.setMessage("Do You Want to Delete this Video");
                            dialog.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    Uri contenturi = ContentUris.withAppendedId(MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                                            Long.parseLong(videolist.get(position).getId()));
                                    File file = new File(videolist.get(position).getPath());
                                    boolean delete = file.delete();
                                    if (delete) {
                                        context.getContentResolver().delete(contenturi, null, null);
                                        videolist.remove(position);
                                        notifyItemRemoved(position);
                                        notifyItemRangeChanged(position, videolist.size());
                                        Toast.makeText(context, "Video Deleted", Toast.LENGTH_SHORT).show();
                                    } else {
                                        Toast.makeText(context, "Can't Deleted", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                            dialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            });
                            dialog.show();
                            bottomSheetDialog.dismiss();
                        }
                    });
                    //properties bottom Sheet
                    sheetview.findViewById(R.id.bs_properties).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            AlertDialog.Builder builder = new AlertDialog.Builder(context);
                            builder.setTitle("Properties");

                            String one = "File: " + videolist.get(position).getDiplayName();
                            String path = videolist.get(position).getPath();
                            int indexOfPath = path.lastIndexOf("/");
                            String two = "Path: " + path.substring(0, indexOfPath);
                            String three = "Size: " + android.text.format.Formatter.formatFileSize(context, Long.parseLong(videolist.get(position).getSize()));
                            String four = "Length: " + timeconversion((long) millisecs);
                            String namewithFormat = videolist.get(position).getDiplayName();
                            int index = namewithFormat.lastIndexOf(".");
                            String format = namewithFormat.substring(index + 1);
                            String five = "Format: " + format;

                            MediaMetadataRetriever metadataRetriever = new MediaMetadataRetriever();
                            metadataRetriever.setDataSource(videolist.get(position).getPath());
                            String height = metadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT);
                            String width = metadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH);

                            String six = "Resolution: " + width + "X" + height;

                            builder.setMessage(one + "\n\n" + two + "\n\n" + three + "\n\n" + four + "\n\n" + five + "\n\n" + six);
                            builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            });
                            builder.show();
                            bottomSheetDialog.dismiss();
                        }
                    });

                    bottomSheetDialog.setContentView(sheetview);
                    bottomSheetDialog.show();
                }
            });

        } else {
            holder.moreoption.setVisibility(View.GONE);
            holder.videoName.setTextColor(Color.WHITE);
            holder.videoSize.setTextColor(Color.WHITE);
        }
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, VideoPlayActivity.class);
                intent.putExtra("position",position);
                intent.putExtra("Videotitle",videolist.get(position).getDiplayName());
                Bundle bundle = new Bundle();
                bundle.putParcelableArrayList("videoArraylist",videolist);
                intent.putExtras(bundle);
                context.startActivity(intent);

                if (viewType == 1) {
                    ((Activity) context).finish();
                }
            }
        });


    }

    @Override
    public int getItemCount() {
        return videolist.size();
    }

    public class VideoViewHolder extends RecyclerView.ViewHolder{

        ImageView thumbnail,moreoption;
        TextView videoName,videoSize,videoDuration;

        public VideoViewHolder(@NonNull View itemView) {
            super(itemView);
            thumbnail = itemView.findViewById(R.id.thumbnail);
            moreoption = itemView.findViewById(R.id.video_moreOption);
            videoName = itemView.findViewById(R.id.video_name);
            videoSize = itemView.findViewById(R.id.video_size);
            videoDuration = itemView.findViewById(R.id.video_duration);
        }
    }
    public String timeconversion(long value){

        String videotime;

        int duration =(int) value;
        int hrs = (duration/3600000);
        int mins = ((duration/60000) % 60000);
        int secs = duration%60000/1000;
        
        if (hrs>0){
            videotime = String.format("%02d:%02d:%02d",hrs,mins,secs);
        }else {
            videotime = String.format("%02d:%02d",mins,secs);

        }
        return videotime;
    }
    public void updateVideoFiles(ArrayList<MediaFiles> files){
        videolist = new ArrayList<>();
        videolist.addAll(files);
        notifyDataSetChanged();
    }
}
