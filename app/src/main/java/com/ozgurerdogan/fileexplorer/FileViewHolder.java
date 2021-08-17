package com.ozgurerdogan.fileexplorer;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

public class FileViewHolder extends RecyclerView.ViewHolder {
    ImageView imageView;
    TextView tvName, tvSize;
    CardView container;


    public FileViewHolder(@NonNull View itemView) {
        super(itemView);

        imageView=itemView.findViewById(R.id.img_fileType);
        tvName=itemView.findViewById(R.id.tv_fileName);
        tvSize=itemView.findViewById(R.id.tvFilesize);
        container=itemView.findViewById(R.id.container);


    }
}
