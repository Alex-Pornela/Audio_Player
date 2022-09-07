package com.activity.audioplayer.View;

import static com.activity.audioplayer.ApplicationClass.ACTION_NEXT;
import static com.activity.audioplayer.ApplicationClass.ACTION_PLAY;
import static com.activity.audioplayer.ApplicationClass.ACTION_PREV;
import static com.activity.audioplayer.ApplicationClass.CHANNEL_ID_2;
import static com.activity.audioplayer.AudioPlayerService.ImageAnimation;
import static com.activity.audioplayer.AudioPlayerService.getAlbum;
import static com.activity.audioplayer.AudioPlayerService.mediaPlayer;
import static com.activity.audioplayer.View.MainActivity.getContextOfApplication;
import static com.activity.audioplayer.View.MainActivity.songArrayList;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.support.v4.media.session.MediaSessionCompat;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.activity.audioplayer.ActionPlaying;
import com.activity.audioplayer.AudioPlayerService;
import com.activity.audioplayer.Model.Song;
import com.activity.audioplayer.NotificationReceiver;
import com.activity.audioplayer.R;
import com.activity.audioplayer.databinding.ActivityPlaySongBinding;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class PlaySong extends AppCompatActivity implements ServiceConnection {

    private ActivityPlaySongBinding binding;
    public static List<Song> listSongs = new ArrayList<>();
    public static int position = -1;
    @SuppressLint("StaticFieldLeak")
    public static TextView title,artist,tvPlayed,duration;
    public static SeekBar seekBar;
    public static ImageView repeat,shuffle,next,prev,thumbnail;
    public static FloatingActionButton playPause;
    boolean like,mBound;
    public static boolean isShuffle = false, isRepeat = false;
     Uri uri;
    AudioPlayerService audioPlayerService;
    public static MediaSessionCompat mediaSessionCompat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate( savedInstanceState );
        binding = ActivityPlaySongBinding.inflate( getLayoutInflater() );
        setContentView( binding.getRoot() );

        initializeView();

        position = getIntent().getIntExtra( "data", -1) ;
        listSongs = songArrayList;
        uri = Uri.parse( listSongs.get( position ).getPath() );

        Intent intent = new Intent(this, AudioPlayerService.class );
        intent.setData(uri);
        startService( intent );
        bindService( intent, this,Context.BIND_AUTO_CREATE );

        initializeSeekBar();

        mediaSessionCompat = new MediaSessionCompat( getBaseContext(), "My Audio");

    }

    private void initializeView() {
        title = binding.songTitle;
        artist = binding.songArtist;
        tvPlayed = binding.tvPlayed;
        duration = binding.tvDuration;
        seekBar = binding.seekBar;
        playPause = binding.plaPauseBtn;
        repeat = binding.repeat;
        shuffle = binding.shuffle;
        next = binding.next;
        prev = binding.prev;
        thumbnail = binding.songThumbnail;

        binding.backBtn.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent( PlaySong.this, MainActivity.class);
                startActivity( intent );
                finish();
            }
        } );

        binding.likeBtn.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!like){
                    binding.likeBtn.setBackgroundTintList( ContextCompat.getColorStateList( getContextOfApplication(),R.color.button_color ) );
                    like = true;
                    Toast.makeText( getContextOfApplication(), "Added to Liked Songs", Toast.LENGTH_SHORT ).show();
                }else {
                    binding.likeBtn.setBackgroundTintList( ContextCompat.getColorStateList( getContextOfApplication(),R.color.secondary_text_color ) );
                    like = false;
                    Toast.makeText( getContextOfApplication(), "Removed from Liked Songs", Toast.LENGTH_SHORT ).show();
                }
            }
        } );

        binding.shuffle.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(isShuffle){
                    binding.shuffle.setImageResource( R.drawable.ic_shuffle_off );
                    isShuffle = false;
                }else{
                    binding.shuffle.setImageResource( R.drawable.ic_shuffle_on );
                    isShuffle = true;

                }
            }
        } );

        binding.repeat.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(isRepeat){
                    isRepeat = false;
                    binding.repeat.setImageResource( R.drawable.ic_repeat_off );
                }else{
                    isRepeat = true;
                    binding.repeat.setImageResource( R.drawable.ic_repeat_on );
                }
            }
        } );
    }

    @Override
    public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
        AudioPlayerService.LocalBinder binder = (AudioPlayerService.LocalBinder) iBinder;
        audioPlayerService = binder.getService();
        if(audioPlayerService != null){
            audioPlayerService.displaySongInfo( position );
            audioPlayerService.updateSeekBar();
            audioPlayerService.showNotification(R.drawable.ic_baseline_pause_24);
        }
        mBound = true;
    }

    @Override
    public void onServiceDisconnected(ComponentName componentName) {
        mBound = false;
    }

    private void initializeSeekBar() {
        seekBar.setMax( mediaPlayer.getDuration() );
        binding.seekBar.setOnSeekBarChangeListener( new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if(mBound){
                    audioPlayerService.seekMediaPlayer();
                }
            }
        } );
    }

    private void prevUiThread() {
       binding.prev.setOnClickListener( new View.OnClickListener() {
           @Override
           public void onClick(View view) {
               if(mBound){
                   audioPlayerService.prevSong();
               }
           }
       } );
    }

    private void nextUiThread() {
        binding.next.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mBound){
                    audioPlayerService.nextSong();
                }
            }
        } );

    }

    private void playUiThread() {
        binding.plaPauseBtn.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mBound){
                    audioPlayerService.playPause();
                }
            }
        } );
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Bind to LocalService
        Intent intent = new Intent(this, AudioPlayerService.class);
        bindService(intent, this, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Intent intent = new Intent(this, AudioPlayerService.class);
        bindService(intent, this, Context.BIND_AUTO_CREATE);
        playUiThread();
        nextUiThread();
        prevUiThread();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(audioPlayerService != null){
            if(mBound){
                unbindService( this );
                mBound = false;
            }
        }

    }

    @Override
    protected void onStop() {
        super.onStop();
        if(audioPlayerService != null){
            if(mBound){
                unbindService( this );
                mBound = false;
            }
        }
    }



}

