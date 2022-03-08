package com.chandru.videoplayer;

import static com.chandru.videoplayer.AccessActivity.REQUEST_PERMISSION_SETTING;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.provider.Settings;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.chandru.videoplayer.adapter.VideoFolderAdapter;
import com.chandru.videoplayer.models.MediaFiles;

import java.nio.file.Path;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private ArrayList<MediaFiles> mediaFiles;
    private ArrayList<String> allFolderslist;
    RecyclerView recyclerView;
    VideoFolderAdapter adapter;
    SwipeRefreshLayout swipeRefreshLayout;

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mediaFiles = new ArrayList<>();
        allFolderslist = new ArrayList<>();

        if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED){
            Toast.makeText(this, "click on permission and allow storage", Toast.LENGTH_LONG).show();
            Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            Uri uri = Uri.fromParts("package",getPackageName(),null);
            intent.setData(uri);
            startActivityForResult(intent,REQUEST_PERMISSION_SETTING);
//            finish();
        }
        recyclerView = findViewById(R.id.foldersRcv);
        swipeRefreshLayout = findViewById(R.id.swipe_refresh);
        showFolders();

        swipeRefreshLayout.setColorSchemeColors(
                getResources().getColor(android.R.color.holo_red_light),
                getResources().getColor(android.R.color.holo_blue_bright),
                getResources().getColor(android.R.color.holo_orange_light),
                getResources().getColor(android.R.color.holo_green_light)
        );

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                showFolders();
                swipeRefreshLayout.setRefreshing(false);
            }
        });

    }

    private void showFolders() {
        mediaFiles = fetchfoldersfromMedia();
//        Toast.makeText(this, fetchMedia().toString(), Toast.LENGTH_LONG).show();
        recyclerView.setLayoutManager(new LinearLayoutManager(this,RecyclerView.VERTICAL,false));
        adapter = new VideoFolderAdapter(this,mediaFiles,allFolderslist);
        adapter.notifyDataSetChanged();
        recyclerView.setAdapter(adapter);

    }

    private ArrayList<MediaFiles> fetchfoldersfromMedia() {

        ArrayList<MediaFiles> mediaFilesArrayList = new ArrayList<>();
        Uri uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;

        Cursor cursor = getContentResolver().query(uri,null,null,null,null);
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

                int index = path.lastIndexOf("/");
                String subString = path.substring(0,index);
                if (!allFolderslist.contains(subString)){
                    allFolderslist.add(subString);
                }
                mediaFilesArrayList.add(mediaFiles);
            }while (cursor.moveToNext());

        }
        return mediaFilesArrayList;

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.fold_menu_items,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        switch (id){
            case R.id.rateus:
                //this can be written in two ways either you can give direct url or
                Uri uri = Uri.parse("https://play.google.com/store/apps/details?id=com.rbpinvestments.rbp");
                // you can use this method
                Uri u = Uri.parse("https://play.google.com/store/apps/details?id="+getApplicationContext().getPackageName());
//                Toast.makeText(this, getApplicationContext().getPackageName(), Toast.LENGTH_LONG).show();
                Intent intent = new Intent(Intent.ACTION_VIEW,uri);
                startActivity(intent);
                break;

            case R.id.refresh:
                finish();
                startActivity(getIntent());
                break;

            case R.id.share:
              Intent share = new Intent();
              share.setAction(Intent.ACTION_SEND);
              //this can be written in two ways either you can give direct url or
//              share.putExtra(Intent.EXTRA_TEXT,"https://play.google.com/store/apps/details?id=com.rbpinvestments.rbp");
                // you can use this method
              share.putExtra(Intent.EXTRA_TEXT,"Check this App Via\n"+"https://play.google.com/store/apps/details?id="+getApplicationContext().getPackageName());
              share.setType("text/plain");
              startActivity(Intent.createChooser(share,"Share Via App\n"));
                break;

            case R.id.exit:
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("Are you sure want to Exit")
                        .setCancelable(false)
                        .setNegativeButton("No",null)
                        .setPositiveButton("yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                finish();
                            }
                        }).create().show();

                break;
        }
        return true;
    }
}