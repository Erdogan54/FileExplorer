package com.ozgurerdogan.fileexplorer;

import android.content.Context;
import android.text.format.Formatter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class FileAdapter extends RecyclerView.Adapter<FileViewHolder>{

    private Context context;
    private ArrayList<File> arrayList;
    private OnFileSelectedListener listener;

    public FileAdapter(Context context, ArrayList<File> arrayList, OnFileSelectedListener onFileSelectedListener) {
        this.context = context;
        this.arrayList = arrayList;
        this.listener=onFileSelectedListener;
    }


    @NonNull
    @Override
    public FileViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view=LayoutInflater.from(context).inflate(R.layout.file_container,parent,false);
        return new FileViewHolder(view);
    }


    @Override
    public void onBindViewHolder(@NonNull FileViewHolder holder, int position) {
        File file=arrayList.get(position);

        holder.tvName.setText(file.getName());
        holder.tvName.setSelected(true);
        int items=0;
        if (arrayList.get(position).isDirectory()){
            File[] files=file.listFiles();

            if (files.length>0){
                for (File singlefile:files){
                    if (!singlefile.isHidden()){
                        items+=1;
                    }
                }

                holder.tvSize.setText(String.valueOf(items)+" files");
            }

        }
        else {
            holder.tvSize.setText(Formatter.formatShortFileSize(context,file.length()));
        }

        if (file.getName().toLowerCase().endsWith(".jpeg")
                || file.getName().toLowerCase().endsWith(".jpg")
                || file.getName().toLowerCase().endsWith(".png"))
            {
            holder.imageView.setImageResource(R.drawable.ic_image);
        }else if (file.getName().toLowerCase().endsWith(".pdf")){
            holder.imageView.setImageResource(R.drawable.ic_pdf);
        }else if (file.getName().toLowerCase().endsWith(".doc")){
            holder.imageView.setImageResource(R.drawable.ic_docs);
        }else if (file.getName().toLowerCase().endsWith(".mp3")
                ||file.getName().toLowerCase().endsWith(".wav"))
        {
            holder.imageView.setImageResource(R.drawable.ic_music);
        }else if(file.getName().toLowerCase().endsWith(".mp4")){
            holder.imageView.setImageResource(R.drawable.ic_play);
        }else if(file.getName().toLowerCase().endsWith(".apk")){
            holder.imageView.setImageResource(R.drawable.ic_android);
        }else{
            holder.imageView.setImageResource(R.drawable.ic_folder );
        }


        holder.container.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onFileClicked(file);

            }
        });
        holder.container.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                listener.onFileLongClicked(file,position);
                return true;
            }
        });
    }


    @Override
    public int getItemCount() {
        return arrayList.size();
    }
}
