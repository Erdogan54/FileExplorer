package com.ozgurerdogan.fileexplorer;

import static android.app.Activity.RESULT_OK;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.format.Formatter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class CategorizedFragment extends Fragment implements OnFileSelectedListener {

    ActivityResultLauncher<Intent> activityResultLauncher;
    ActivityResultLauncher<String> permissionLauncher;
    private RecyclerView recyclerView;
    private ArrayList<File> fileArrayList;

    private FileAdapter fileAdapter;
    File storageFileOrFiles;
    File path;
    String selectFileOrFilesPath;
    String [] items={"Details","Rename","Delete","Share"};

    View view;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view=inflater.inflate(R.layout.categorized_fragment,container,false);

        runtimePermission();

        return view;
    }

    private void runtimePermission() {
        Dexter.withContext(getContext()).withPermissions(
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .withListener(new MultiplePermissionsListener() {
                    @Override
                    public void onPermissionsChecked(MultiplePermissionsReport multiplePermissionsReport) {

                        if(multiplePermissionsReport.areAllPermissionsGranted()){
                            try {
                                displayFiles();
                            }catch (Exception e){
                                Toast.makeText(getContext(),e.getLocalizedMessage(),Toast.LENGTH_LONG).show();
                            }

                        }else{
                            Toast.makeText(getContext(), "You must give permission to view the files.", Toast.LENGTH_SHORT).show();
                        }

                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(List<PermissionRequest> list, PermissionToken permissionToken) {
                        permissionToken.continuePermissionRequest();
                    }
                }).check();

    }

    private void displayFiles() {
        fileArrayList=new ArrayList<>();
        recyclerView=view.findViewById(R.id.recycler_internal);
        recyclerView.setHasFixedSize(true);

        Bundle bundle=this.getArguments();
        if(bundle.getString("fileType").equals("downloads")){
            path=Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        }else{
            path=Environment.getExternalStorageDirectory();
        }

        storageFileOrFiles=path;


        try {
            fileArrayList.addAll(findFiles(storageFileOrFiles));
            recyclerView.setLayoutManager(new GridLayoutManager(getContext(),3));
            fileAdapter=new FileAdapter(getContext(),fileArrayList,this);
            recyclerView.setAdapter(fileAdapter);

        }catch (Exception e){
            e.printStackTrace();
        }



    }
    public ArrayList<File> findFiles (File file){

        ArrayList<File> arrayList=new ArrayList<>();

        for (File singlefile:file.listFiles()){
            if (singlefile.isAbsolute() && !singlefile.isHidden()){
                arrayList.addAll(findFiles(singlefile));
            }else{
                switch (getArguments().getString("fileType")){
                    case "image":
                        if (singlefile.getName().toLowerCase().endsWith(".jpeg")
                                || singlefile.getName().toLowerCase().endsWith(".jpg")
                                || singlefile.getName().toLowerCase().endsWith(".png")){
                            arrayList.add(singlefile);
                        }
                        break;
                    case "video":
                        if (singlefile.getName().toLowerCase().endsWith(".mp4")){
                            arrayList.add(singlefile);
                        }
                        break;
                    case "music":
                        if (singlefile.getName().toLowerCase().endsWith(".mp3")
                                || singlefile.getName().toLowerCase().endsWith(".wav")){
                            arrayList.add(singlefile);
                        }
                        break;
                    case "docs":
                        if (singlefile.getName().toLowerCase().endsWith(".pdf")
                                || singlefile.getName().toLowerCase().endsWith(".doc")){
                            arrayList.add(singlefile);
                        }
                        break;
                    case "apk":
                        if (singlefile.getName().toLowerCase().endsWith(".apk")){
                            arrayList.add(singlefile);
                        }
                        break;
                    case "downloads":
                        if (singlefile.getName().toLowerCase().endsWith(".jpeg")
                                || singlefile.getName().toLowerCase().endsWith(".jpg")
                                || singlefile.getName().toLowerCase().endsWith(".png")
                                || singlefile.getName().toLowerCase().endsWith(".mp3")
                                || singlefile.getName().toLowerCase().endsWith(".mp4")
                                || singlefile.getName().toLowerCase().endsWith(".wav")
                                || singlefile.getName().toLowerCase().endsWith(".pdf")
                                || singlefile.getName().toLowerCase().endsWith(".doc")
                                || singlefile.getName().toLowerCase().endsWith(".apk")){
                            arrayList.add(singlefile);
                        }
                        break;

                }
            }

        }

        return arrayList;
    }

    @Override
    public void onFileClicked(File file) {

        if (file.isDirectory()){
            Bundle bundle=new Bundle();
            bundle.putString("path",file.getAbsolutePath());

            CategorizedFragment categorizedFragment=new CategorizedFragment();
            categorizedFragment.setArguments(bundle);

            getFragmentManager().beginTransaction().replace(R.id.fragment_container,categorizedFragment).commit();
        }else{

            try {
                FileOpener.openFile(getContext(),file);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    @Override
    public void onFileLongClicked(File file,int position_file) {
        final Dialog optionsDialog=new Dialog(getContext());
        optionsDialog.setContentView(R.layout.option_dialog);
        optionsDialog.setTitle("Select Options");
        ListView options=optionsDialog.findViewById(R.id.list);
        CustomAdapter customAdapter=new CustomAdapter();
        options.setAdapter(customAdapter);
        optionsDialog.show();

        options.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String selectedItem=parent.getItemAtPosition(position).toString();

                switch (selectedItem){
                    case "Details":
                        AlertDialog.Builder detailDialog= new AlertDialog.Builder(getContext());
                        detailDialog.setTitle("Details");
                        final TextView details=new TextView(getContext());
                        detailDialog.setView(details);
                        Date lastModified=new Date(file.lastModified());
                        SimpleDateFormat formatter=new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
                        String formattedDate=formatter.format(lastModified);

                        details.setText("File Name: "+file.getName()+"\n"+
                        "Size: "+ Formatter.formatShortFileSize(getContext(),file.length())+"\n"+
                                "Path: "+file.getAbsolutePath()+"\n"+
                                "Last Modified: "+formattedDate);

                        detailDialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                optionsDialog.cancel();
                            }
                        });

                        AlertDialog alertDialog_details= detailDialog.create();
                        alertDialog_details.show();
                        break;

                    case "Rename":

                        AlertDialog.Builder renameDialog=new AlertDialog.Builder(getContext());
                        renameDialog.setTitle("Rename File:");
                        final EditText name=new EditText(getContext());
                        renameDialog.setView(name);

                        renameDialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                try{
                                    File current=new File(file.getAbsolutePath());
                                    String new_name=name.getEditableText().toString();
                                    String extention=file.getAbsolutePath().substring(file.getAbsolutePath().lastIndexOf("."));
                                    File destination=new File(file.getAbsolutePath().replace(file.getName(),new_name)+extention);
                                    if(current.renameTo(destination)){
                                        fileArrayList.set(position_file,destination);
                                        fileAdapter.notifyItemChanged(position_file);
                                        Toast.makeText(getContext(), "Renamed!!", Toast.LENGTH_SHORT).show();

                                    }else{
                                        Toast.makeText(getContext(), "Couldn't Rename!", Toast.LENGTH_SHORT).show();
                                    }
                                    optionsDialog.cancel();
                                }catch (Exception e){
                                    e.printStackTrace();
                                    optionsDialog.cancel();
                                }

                            }
                        });
                        renameDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                optionsDialog.cancel();
                            }
                        });
                        renameDialog.create().show();
                        break;


                    case "Delete":
                        System.out.println("delete tıklandı");
                        AlertDialog.Builder deleteDialog=new AlertDialog.Builder(getContext());
                        deleteDialog.setTitle("Delete"+file.getName()+"?");
                        deleteDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                try{

                                    boolean del=file.delete();
                                    if (del){
                                        fileArrayList.remove(position_file);
                                        fileAdapter.notifyDataSetChanged();

                                        Toast.makeText(getContext(), "File Deleted!!", Toast.LENGTH_SHORT).show();
                                    }else{
                                        Toast.makeText(getContext(), "File could not be deleted!!", Toast.LENGTH_SHORT).show();
                                    }

                                }catch (Exception e){
                                    e.printStackTrace();
                                }
                                optionsDialog.cancel();

                            }
                        });
                        deleteDialog.setNegativeButton("No", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                optionsDialog.cancel();
                            }
                        });
                        deleteDialog.create().show();
                        break;

                    case "Share":
                        try{
                            String fileName=file.getName();
                            Intent share=new Intent();
                            share.setAction(Intent.ACTION_SEND);
                            share.setType("image/jpeg");
                            share.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(file));
                            startActivity(Intent.createChooser(share,"Share "+fileName));


                        }catch (Exception e){
                            e.printStackTrace();
                        }
                        optionsDialog.cancel();
                        break;
                }
            }
        });
    }

    class CustomAdapter extends BaseAdapter{


        @Override
        public int getCount() {
            return items.length;
        }

        @Override
        public Object getItem(int position) {
            return items[position];
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }


        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view=getLayoutInflater().inflate(R.layout.option_layout,null);
            TextView txtoptions=view.findViewById(R.id.txtOptions);
            ImageView imgView=view.findViewById(R.id.imgOption);
            txtoptions.setText(items[position]);

            if (items[position].equals("Details")){
                imgView.setImageResource(R.drawable.ic_details);
            }else if (items[position].equals("Rename")){
                imgView.setImageResource(R.drawable.ic_rename);
            }else if (items[position].equals("Delete")){
                imgView.setImageResource(R.drawable.ic_delete);
            }else{
                imgView.setImageResource(R.drawable.ic_share);
            }

            return view;
        }
    }
}
