package com.chandru.videoplayer;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.PictureInPictureParams;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.media.MediaMetadataRetriever;
import android.media.audiofx.AudioEffect;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.util.Rational;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.chandru.videoplayer.adapter.PlayBackIconAdapter;
import com.chandru.videoplayer.adapter.VideoFilesAdapter;
import com.chandru.videoplayer.models.IconModel;
import com.chandru.videoplayer.models.MediaFiles;
import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.PlaybackParameters;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.source.ConcatenatingMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.ProgressiveMediaSource;
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;

import java.io.File;
import java.util.ArrayList;

public class VideoPlayActivity extends AppCompatActivity implements View.OnClickListener {

    PlayerView playerView;
    SimpleExoPlayer player;
    int position;
    String videotitle,nwTitle;
    ArrayList<MediaFiles> videoFiles;
    VideoFilesAdapter videoFilesAdapter;
    TextView vdeotitle;
    private ControlsMode controlsMode;
    PlaybackParameters parameters;
    float speed;

    public enum ControlsMode {
        LOCK, FULLSCREEN;
    }

    ImageView videoBack, lock, unlock, scaling,videoList;
    RelativeLayout root;
    ImageView nextbtn, previousbtn;
    ConcatenatingMediaSource concatenatingMediaSource;

    //horizontal recycleview var
    public ArrayList<IconModel> iconModelArrayList;
    public PlayBackIconAdapter playBackIconAdapter;
    public RecyclerView recyclerViewIcons;
    boolean expand = false;
    View nightMode;
    boolean dark = false;
    boolean mute = false;
    PictureInPictureParams.Builder pictureinpicture;
    boolean ischecked;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setFullScreen();
        setContentView(R.layout.activity_video_play);
        getSupportActionBar().hide();
        videoFiles = new ArrayList<>();
        position = getIntent().getIntExtra("position", 1);
        videotitle = getIntent().getStringExtra("Videotitle");
        videoFiles = getIntent().getExtras().getParcelableArrayList("videoArraylist");
        screenOrientation();

        playerView = findViewById(R.id.exoplayer);
        nextbtn = findViewById(R.id.exo_next);
        previousbtn = findViewById(R.id.exo_prev);
        vdeotitle = findViewById(R.id.video_title);
        videoBack = findViewById(R.id.video_back);
        lock = findViewById(R.id.lock);
        unlock = findViewById(R.id.unlock);
        scaling = findViewById(R.id.scale);
        videoList = findViewById(R.id.video_list);
        root = findViewById(R.id.root_layout);
        recyclerViewIcons = findViewById(R.id.recycleview);
        nightMode = findViewById(R.id.night_mode);

        vdeotitle.setText(videotitle);


        nextbtn.setOnClickListener(this);
        previousbtn.setOnClickListener(this);
        videoBack.setOnClickListener(this);
        lock.setOnClickListener(this);
        unlock.setOnClickListener(this);
        videoList.setOnClickListener(this);
        scaling.setOnClickListener(firstlistener);

        iconModelArrayList = new ArrayList<>();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            pictureinpicture = new PictureInPictureParams.Builder();
        }

        iconModelArrayList.add(new IconModel(R.drawable.ic_right, ""));
        iconModelArrayList.add(new IconModel(R.drawable.ic_night_mode, "Night"));
        iconModelArrayList.add(new IconModel(R.drawable.ic_pic_in_pic, "PopUpMode"));
        iconModelArrayList.add(new IconModel(R.drawable.ic_rotate, "Rotate"));

        playBackIconAdapter = new PlayBackIconAdapter(this, iconModelArrayList);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this, RecyclerView.HORIZONTAL, true);
        recyclerViewIcons.setLayoutManager(layoutManager);
        recyclerViewIcons.setAdapter(playBackIconAdapter);
        playBackIconAdapter.notifyDataSetChanged();
        playBackIconAdapter.setOnItemClickListener(new PlayBackIconAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                if (position == 0) {
                    if (expand) {
                        iconModelArrayList.clear();
                        iconModelArrayList.add(new IconModel(R.drawable.ic_right, ""));
                        iconModelArrayList.add(new IconModel(R.drawable.ic_night_mode, "Night"));
                        iconModelArrayList.add(new IconModel(R.drawable.ic_pic_in_pic, "PopUpMode"));
                        iconModelArrayList.add(new IconModel(R.drawable.ic_rotate, "Rotate"));
                        playBackIconAdapter.notifyDataSetChanged();
                        expand = false;
                    } else {
                        if (iconModelArrayList.size() == 4) {
                            iconModelArrayList.add(new IconModel(R.drawable.ic_volume_off, "Mute"));
                            iconModelArrayList.add(new IconModel(R.drawable.ic_volume_, "Volume"));
                            iconModelArrayList.add(new IconModel(R.drawable.ic_brightness, "Brightness"));
                            iconModelArrayList.add(new IconModel(R.drawable.ic_speed, "Speed"));
                            iconModelArrayList.add(new IconModel(R.drawable.ic_equalizer, "Equalizer"));
                            iconModelArrayList.add(new IconModel(R.drawable.ic_subtitles, "Subtitle"));
                        }
                        iconModelArrayList.set(position, new IconModel(R.drawable.ic_left, ""));
                        playBackIconAdapter.notifyDataSetChanged();
                        expand = true;
                    }
                }
                if (position == 1) {
                    //nightMode
                    if (dark) {
                        nightMode.setVisibility(View.GONE);
                        iconModelArrayList.set(position,new IconModel(R.drawable.ic_night_mode,"Night"));
                        playBackIconAdapter.notifyDataSetChanged();
                        dark = false;

                    } else {
                        nightMode.setVisibility(View.VISIBLE);
                        iconModelArrayList.set(position,new IconModel(R.drawable.ic_night_mode,"Day"));
                        playBackIconAdapter.notifyDataSetChanged();
                        dark = true;
                    }
                }
                if (position == 2) {
                    //Popupmode
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        Rational ratio = new Rational(16, 9);
                        pictureinpicture.setAspectRatio(ratio);
                        enterPictureInPictureMode(pictureinpicture.build());
                    } else {
                        Log.wtf("Not oreo","yes");
                        
                    }


                }
                if (position == 3) {
                    //rotatescreen
                    if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
                        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                        playBackIconAdapter.notifyDataSetChanged();
                    } else if (getResources().getConfiguration().orientation== Configuration.ORIENTATION_LANDSCAPE) {
                        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                        playBackIconAdapter.notifyDataSetChanged();
                    }
                }
                if (position == 4) {
                    //mutevolume
                    if (mute) {
                        player.setVolume(100);
                        iconModelArrayList.set(position, new IconModel(R.drawable.ic_volume_off, "Mute"));
                        playBackIconAdapter.notifyDataSetChanged();

                        mute = false;

                    } else {
                        player.setVolume(0);
                        iconModelArrayList.set(position, new IconModel(R.drawable.ic_volume_, "unMute"));
                        playBackIconAdapter.notifyDataSetChanged();
                        mute = true;
                    }


                }
                if (position == 5) {
                    //volumecontrol
                    VolumeDialog volumeDialog = new VolumeDialog();
                    volumeDialog.show(getSupportFragmentManager(),"dialog");
                    playBackIconAdapter.notifyDataSetChanged();




                }
                if (position == 6) {
                    //brightnesscontrol
                    BrightnessDialog brightnessDialog = new BrightnessDialog();
                    brightnessDialog.show(getSupportFragmentManager(),"dialog");
                    playBackIconAdapter.notifyDataSetChanged();


                }

                if (position == 7) {
                    //playbackspeed
                    AlertDialog.Builder builder = new AlertDialog.Builder(VideoPlayActivity.this);
                    builder.setTitle("Select PlayBack Speed").setPositiveButton("ok",null);
                    String[] items = {"0.5x","1x Normal speed","1.5x","2x"};
                    int checkedItem = -1;
                    builder.setSingleChoiceItems(items, checkedItem, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            switch (which) {
                                case 0:
                                    speed = 0.5f;
                                    parameters = new PlaybackParameters(speed);
                                    player.setPlaybackParameters(parameters);
                                    break;
                                case 1:
                                    speed = 1f;
                                    parameters = new PlaybackParameters(speed);
                                    player.setPlaybackParameters(parameters);
                                    break;
                                case 2:
                                    speed = 1.5f;
                                    parameters = new PlaybackParameters(speed);
                                    player.setPlaybackParameters(parameters);
                                    break;
                                case 3:
                                    speed = 2.0f;
                                    parameters = new PlaybackParameters(speed);
                                    player.setPlaybackParameters(parameters);
                                    break;
                            }
                        }
                    });
                    AlertDialog alert = builder.create();
                    alert.show();
                }
                if (position == 8) {
                    //Equalizer
                    Intent intent = new Intent(AudioEffect.ACTION_DISPLAY_AUDIO_EFFECT_CONTROL_PANEL);
                    if (intent.resolveActivity(getPackageManager()) != null) {
                        startActivityForResult(intent, 123);
                    } else {
                        Toast.makeText(VideoPlayActivity.this,"NO Equalizer Found",Toast.LENGTH_SHORT).show();

                    }
                    playBackIconAdapter.notifyDataSetChanged();
                }
                if (position == 9) {
                    //subtitle
                    Toast.makeText(VideoPlayActivity.this, "subtitle", Toast.LENGTH_SHORT).show();
                }
            }
        });


        playVideo();

    }

    private void playVideo() {
        String path = videoFiles.get(position).getPath();
        Uri uri = Uri.parse(path);
        player = new SimpleExoPlayer.Builder(this).build();
        DefaultDataSourceFactory dataSourceFactory = new DefaultDataSourceFactory(this, Util.getUserAgent(this, "app"));
        concatenatingMediaSource = new ConcatenatingMediaSource();
        for (int i = 0; i < videoFiles.size(); i++) {
            new File(String.valueOf(videoFiles.get(i)));
            MediaSource mediaSource = new ProgressiveMediaSource.Factory(dataSourceFactory)
                    .createMediaSource(Uri.parse(String.valueOf(uri)));
            concatenatingMediaSource.addMediaSource(mediaSource);
        }
        playerView.setPlayer(player);
        playerView.setKeepScreenOn(true);
        player.setPlaybackParameters(parameters);
        player.prepare(concatenatingMediaSource);
        player.seekTo(position, C.TIME_UNSET);
        playError();
    }

    private void screenOrientation() {
        try {
            MediaMetadataRetriever metadataRetriever = new MediaMetadataRetriever();
            Bitmap bitmap;
            String path = videoFiles.get(position).getPath();
            Uri uri = Uri.parse(path);
            metadataRetriever.setDataSource(this,uri);
            bitmap = metadataRetriever.getFrameAtTime();

            int videoWidth = bitmap.getWidth();
            int videoHeight = bitmap.getHeight();

            if (videoWidth > videoHeight) {
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
            } else {
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void playError() {
        player.addListener(new Player.Listener() {
            @Override
            public void onPlayerError(ExoPlaybackException error) {
                Toast.makeText(VideoPlayActivity.this, "Video Playing Error!!!", Toast.LENGTH_SHORT).show();
//                Player.Listener.super.onPlayerError(error);
            }
        });
        player.setPlayWhenReady(true);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (player.isPlaying()) {
            player.stop();
        }
        finish();
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onPause() {
        super.onPause();
        player.setPlayWhenReady(false);
        player.getPlaybackState();
        if (isInPictureInPictureMode()) {
            player.setPlayWhenReady(true);
        } else {
            player.setPlayWhenReady(false);
            player.getPlaybackState();

        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        player.setPlayWhenReady(true);
        player.getPlaybackState();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        player.setPlayWhenReady(true);
        player.getPlaybackState();
    }

    private void setFullScreen() {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);


    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.video_back:
                if (player != null) {
                    player.release();
                }
                finish();
                break;

            case R.id.video_list:
                PlayListDialog playListDialog = new PlayListDialog(videoFiles,videoFilesAdapter);
                playListDialog.show(getSupportFragmentManager(),playListDialog.getTag());
                break;

            case R.id.lock:
                controlsMode = ControlsMode.FULLSCREEN;
                root.setVisibility(View.VISIBLE);
                lock.setVisibility(View.INVISIBLE);
//                Toast.makeText(this,"UnLocked" ,Toast.LENGTH_SHORT).show();
                Toast toast = Toast.makeText(this, "UnLocked", Toast.LENGTH_SHORT);
                View view = toast.getView();
                TextView view1 = view.findViewById(android.R.id.message);
                view1.setTextSize(16);
                view1.setTextColor(Color.CYAN);
                toast.show();

                break;
            case R.id.unlock:
                controlsMode = ControlsMode.LOCK;
                root.setVisibility(View.INVISIBLE);
                lock.setVisibility(View.VISIBLE);
//                Toast.makeText(this, "Locked", Toast.LENGTH_SHORT).show();
                Toast toast1 = Toast.makeText(this, "UnLocked", Toast.LENGTH_SHORT);
                View view0 = toast1.getView();
                TextView view2 = view0.findViewById(android.R.id.message);
                view2.setTextSize(16);
                view2.setTextColor(Color.MAGENTA);
                toast1.show();
                break;


            case R.id.exo_next:
                try {
                    player.stop();
                    position++;
                    nwTitle = videoFiles.get(position).getDiplayName();
                    vdeotitle.setText(nwTitle);
                    playVideo();
                } catch (Exception e) {
                    Toast.makeText(this, "No Next Video", Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                }
                break;

            case R.id.exo_prev:
                try {
                    player.stop();
                    position--;
                    nwTitle = videoFiles.get(position).getDiplayName();
                    vdeotitle.setText(nwTitle);
                    playVideo();
                } catch (Exception e) {
                    Toast.makeText(this, "No previous Video", Toast.LENGTH_SHORT).show();

                    e.printStackTrace();
                }
                break;
        }
    }

    View.OnClickListener firstlistener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            playerView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_FILL);
            player.setVideoScalingMode(C.VIDEO_SCALING_MODE_DEFAULT);
            scaling.setImageResource(R.drawable.fullscreen);

//            Toast.makeText(VideoPlayActivity.this,"Full Screen",Toast.LENGTH_SHORT).show();
            Toast toast = Toast.makeText(VideoPlayActivity.this, "Full Screen", Toast.LENGTH_SHORT);
            View view = toast.getView();
            TextView view1 = view.findViewById(android.R.id.message);
            view1.setTextColor(Color.RED);
            view1.setTextSize(16);
            toast.show();
            scaling.setOnClickListener(secondlistener);
        }
    };

    View.OnClickListener secondlistener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            playerView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_ZOOM);
            player.setVideoScalingMode(C.VIDEO_SCALING_MODE_DEFAULT);
            scaling.setImageResource(R.drawable.zoom);

//            Toast.makeText(VideoPlayActivity.this,"Zoom",Toast.LENGTH_SHORT).show();
            Toast toast = Toast.makeText(VideoPlayActivity.this, "Zoom", Toast.LENGTH_SHORT);
            View view = toast.getView();
            TextView view1 = view.findViewById(android.R.id.message);
            view1.setTextSize(16);
            view1.setTextColor(Color.GREEN);
            toast.show();
            scaling.setOnClickListener(thirdlistener);
        }
    };

    View.OnClickListener thirdlistener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            playerView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_FIT);
            player.setVideoScalingMode(C.VIDEO_SCALING_MODE_DEFAULT);
            scaling.setImageResource(R.drawable.fit);

//            Toast.makeText(VideoPlayActivity.this,"Fit",Toast.LENGTH_SHORT).show();
            Toast toast = Toast.makeText(VideoPlayActivity.this, "Fit", Toast.LENGTH_SHORT);
            View view = toast.getView();
            TextView view1 = view.findViewById(android.R.id.message);
            view1.setTextColor(Color.BLUE);
            view1.setTextSize(16);
            toast.show();
            scaling.setOnClickListener(firstlistener);
        }
    };

    @Override
    public void onPictureInPictureModeChanged(boolean isInPictureInPictureMode, Configuration newConfig) {
        super.onPictureInPictureModeChanged(isInPictureInPictureMode, newConfig);
        ischecked = isInPictureInPictureMode;
        if (isInPictureInPictureMode) {
            playerView.hideController();
        } else {
            playerView.showController();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (ischecked) {
            player.release();
            finish();
        }
    }
}