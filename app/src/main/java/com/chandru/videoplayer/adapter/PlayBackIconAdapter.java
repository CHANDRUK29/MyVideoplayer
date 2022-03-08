package com.chandru.videoplayer.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.chandru.videoplayer.R;
import com.chandru.videoplayer.models.IconModel;

import java.util.ArrayList;

public class PlayBackIconAdapter extends RecyclerView.Adapter<PlayBackIconAdapter.IconViewHolder> {
    public Context context;
   public ArrayList<IconModel> iconModels;
   public OnItemClickListener mlistener;

    public interface OnItemClickListener {
        void onItemClick(int position);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        mlistener = listener;
    }

    public PlayBackIconAdapter(Context context, ArrayList<IconModel> iconModels) {
        this.context = context;
        this.iconModels = iconModels;
    }

    @NonNull
    @Override
    public IconViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.icons_layout,parent,false);
        return new IconViewHolder(view,mlistener);
    }

    @Override
    public void onBindViewHolder(@NonNull IconViewHolder holder, int position) {
        holder.icon.setImageResource(iconModels.get(position).getImageView());
        holder.iconName.setText(iconModels.get(position).getIconTitile());

    }

    @Override
    public int getItemCount() {
        return iconModels.size();
    }

    public class IconViewHolder extends RecyclerView.ViewHolder{

        public TextView iconName;
       public ImageView icon;

        public IconViewHolder(@NonNull View itemView,OnItemClickListener listener) {
            super(itemView);
            icon = itemView.findViewById(R.id.playback_icon);
            iconName = itemView.findViewById(R.id.icon_title);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (listener != null) {
                        int position = getAdapterPosition();
                        if (position != RecyclerView.NO_POSITION) {
                            listener.onItemClick(position);
                        }
                    }
                }
            });


        }
    }
}
