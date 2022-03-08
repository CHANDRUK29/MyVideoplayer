package com.chandru.videoplayer;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.SearchView;

import com.chandru.videoplayer.adapter.VideoFilesAdapter;
import com.chandru.videoplayer.models.MediaFiles;

import java.util.ArrayList;

public class VideoFilesActivity extends AppCompatActivity implements SearchView.OnQueryTextListener {

    private static final String MY_PREF = "my preference";
    RecyclerView recyclerView;
    private ArrayList<MediaFiles> videoFilesArrayList;
    public static VideoFilesAdapter videoFilesAdapter;
    String folder_name;
    String sortOrder;
    SwipeRefreshLayout swipeRefreshLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_files);

        folder_name = getIntent().getStringExtra("folderName");
        getSupportActionBar().setTitle(folder_name);
        recyclerView = findViewById(R.id.videosRCV);
        swipeRefreshLayout = findViewById(R.id.refresh_videos);

        SharedPreferences.Editor editor = getSharedPreferences(MY_PREF,MODE_PRIVATE).edit();
        editor.putString("playlistFolderName",folder_name);
        editor.apply();

        showvideosfromfolder();

        swipeRefreshLayout.setColorSchemeColors(
                getResources().getColor(android.R.color.holo_red_light),
                getResources().getColor(android.R.color.holo_blue_bright),
                getResources().getColor(android.R.color.holo_orange_light),
                getResources().getColor(android.R.color.holo_green_light)
        );


        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                swipeRefreshLayout.setRefreshing(false);
                showvideosfromfolder();

            }
        });
    }

    private void showvideosfromfolder() {
        videoFilesArrayList = fetchvideosfromfolder(folder_name);
        recyclerView.setLayoutManager(new LinearLayoutManager(this,RecyclerView.VERTICAL,false));
        videoFilesAdapter = new VideoFilesAdapter(this,videoFilesArrayList,0);
        videoFilesAdapter.notifyDataSetChanged();
        recyclerView.setAdapter(videoFilesAdapter);
    }

    private ArrayList<MediaFiles> fetchvideosfromfolder(String folderName) {
        SharedPreferences preferences = getSharedPreferences(MY_PREF,MODE_PRIVATE);
        String Sort = preferences.getString("Sort","abcd");

        ArrayList<MediaFiles> videofiles = new ArrayList<>();
        Uri uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
        if (Sort.equals("SortName")) {
            sortOrder = MediaStore.MediaColumns.DISPLAY_NAME+" ASC";
        } else if (Sort.equals("SortSize")) {
            sortOrder = MediaStore.MediaColumns.SIZE+" DESC";
        }
        else if (Sort.equals("SortDate")) {
            sortOrder = MediaStore.MediaColumns.DATE_ADDED+" DESC";
        }
        else if (Sort.equals("SortLength")) {
            sortOrder = MediaStore.MediaColumns.DURATION+" DESC";
        }
        String selection = MediaStore.Video.Media.DATA+" like?";
        String[] selectionArr = new String[]{"%"+folderName+"%"};
        Cursor cursor = getContentResolver().query(uri,null,selection,selectionArr,sortOrder);
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.video_menu,menu);
        MenuItem menuItem = menu.findItem(R.id.search_video);
        SearchView searchView = (SearchView) menuItem.getActionView();
        searchView.setOnQueryTextListener(this);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        SharedPreferences sharedPreferences = getSharedPreferences(MY_PREF, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        int id = item.getItemId();
        switch (id) {
            case R.id.refresh_files:
                finish();
                startActivity(getIntent());
                break;

            case R.id.sort_by:
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("Sort By");
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        editor.apply();
                        finish();
                        startActivity(getIntent());
                        dialog.dismiss();
                    }
                });
                String[] items = {"Name (A to Z)","Size (Big to Small)","Date (New to Old)","Length (Long to Short)"};
                builder.setSingleChoiceItems(items, -1, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case 0:
                                editor.putString("Sort","SortName");
                                break;

                            case 1:
                                editor.putString("Sort","SortSize");
                                break;

                            case 2:
                                editor.putString("Sort","SortDate");
                                break;

                            case 3:
                                editor.putString("Sort","SortLength");
                        }
                    }
                });
                builder.create().show();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        String input = newText.toLowerCase();
        ArrayList<MediaFiles> mediaFiles = new ArrayList<>();
        for (MediaFiles media:videoFilesArrayList) {
            if (media.getTitle().toLowerCase().contains(input)){
                mediaFiles.add(media);
            }
            
        }
        VideoFilesActivity.videoFilesAdapter.updateVideoFiles(mediaFiles);
        return true;
    }
}