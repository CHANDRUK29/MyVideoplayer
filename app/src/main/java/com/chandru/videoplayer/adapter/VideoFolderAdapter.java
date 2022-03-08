package com.chandru.videoplayer.adapter;

import android.content.Context;
import android.content.Intent;
import android.provider.ContactsContract;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.chandru.videoplayer.R;
import com.chandru.videoplayer.VideoFilesActivity;
import com.chandru.videoplayer.models.MediaFiles;

import java.util.ArrayList;

public class VideoFolderAdapter extends RecyclerView.Adapter<VideoFolderAdapter.MyViewHolder>{
    private Context context;
    private ArrayList<MediaFiles> mediaFiles;
    private ArrayList<String> folderPath;

    public VideoFolderAdapter(Context context, ArrayList<MediaFiles> mediaFiles, ArrayList<String> folderPath) {
        this.context = context;
        this.mediaFiles = mediaFiles;
        this.folderPath = folderPath;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.folder_item,parent,false);
        return new MyViewHolder(view);
//        **alternate for above line**
//        MyViewHolder myViewHolder = new MyViewHolder(view);
//        return myViewHolder;

    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        int indexpath = folderPath.get(position).lastIndexOf("/");
        String nameofFolder = folderPath.get(position).substring(indexpath+1);
//        Toast.makeText(context,nameofFolder, Toast.LENGTH_LONG).show();

        holder.folderName.setText(nameofFolder);
        holder.FolderPath.setText(folderPath.get(position));
        holder.noofFiles.setText(noOFFolders(folderPath.get(position)) +" Videos");

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, VideoFilesActivity.class);
                intent.putExtra("folderName",nameofFolder);
                context.startActivity(intent);

            }
        });

    }

    @Override
    public int getItemCount() {
        return folderPath.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        TextView folderName,FolderPath,noofFiles;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            folderName = itemView.findViewById(R.id.folderName);
            FolderPath = itemView.findViewById(R.id.folderPath);
            noofFiles = itemView.findViewById(R.id.noofFiles);
        }

    }
    int noOFFolders(String folder_name){
        int filenos =0;
        for (MediaFiles mediaFls: mediaFiles) {
            if (mediaFls.getPath().substring(0,mediaFls.getPath().lastIndexOf("/")).endsWith(folder_name)){
                filenos++;
            }
        }
        return filenos;
    }
}
