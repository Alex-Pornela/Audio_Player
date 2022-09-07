package com.activity.audioplayer;

import static com.activity.audioplayer.ApplicationClass.ACTION_CLOSE;
import static com.activity.audioplayer.ApplicationClass.ACTION_NEXT;
import static com.activity.audioplayer.ApplicationClass.ACTION_PLAY;
import static com.activity.audioplayer.ApplicationClass.ACTION_PREV;
import static com.activity.audioplayer.ApplicationClass.CHANNEL_ID_2;
import static com.activity.audioplayer.View.MainActivity.songArrayList;
import static com.activity.audioplayer.View.PlaySong.artist;
import static com.activity.audioplayer.View.PlaySong.duration;
import static com.activity.audioplayer.View.PlaySong.isRepeat;
import static com.activity.audioplayer.View.PlaySong.isShuffle;
import static com.activity.audioplayer.View.PlaySong.mediaSessionCompat;
import static com.activity.audioplayer.View.PlaySong.playPause;
import static com.activity.audioplayer.View.PlaySong.position;
import static com.activity.audioplayer.View.PlaySong.seekBar;
import static com.activity.audioplayer.View.PlaySong.thumbnail;
import static com.activity.audioplayer.View.PlaySong.title;
import static com.activity.audioplayer.View.PlaySong.tvPlayed;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.activity.audioplayer.Model.Song;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class AudioPlayerService extends Service implements MediaPlayer.OnCompletionListener{
    public static List<Song> listSongs = new ArrayList<>();
    public static MediaPlayer mediaPlayer = new MediaPlayer();
    private Handler handle;
    public static Uri mUri;
    private final IBinder binder = new LocalBinder();
    public static final String SONG_LAST_PLAYED = "LAST_PLAYED";
    public static final String MUSIC_FILE = "MUSIC_URI";
    public static final String ARTIST_NAME = "ARTIST NAME";
    public static final String SONG_TITLE = "SONG TITLE";


    public class LocalBinder extends Binder{
        public AudioPlayerService getService(){
            return AudioPlayerService.this;
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        handle = new Handler( );

        mUri = intent.getData() ;
        if (mUri != null){
            createMediaPlayer(mUri);
        }
        listSongs = songArrayList;

        String actionName = intent.getStringExtra( "ActionName" );
        if(actionName != null){
            switch (actionName){
                case "playPause":
                    playPause();
                    break;
                case "next":
                    nextSong();
                    break;
                case "previous":
                    prevSong();
                    break;
                case "close":
                    if(mediaPlayer.isPlaying()){
                        stopForeground( true );
                        mediaPlayer.pause();
                        playPause.setImageResource( R.drawable.ic_baseline_play_arrow_24 );
                    }else{
                        stopForeground( true );

                    }
                    /*stopForeground( true );
                   stopSelf();*/
                    break;
            }
        }

        return START_STICKY;
    }


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    public void runOnUiThread(Runnable runnable){
        handle.post( runnable);
    }

    private void createMediaPlayer(Uri uri) {
        if (mediaPlayer != null){
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = MediaPlayer.create( getApplicationContext(),uri );
            mediaPlayer.start();
        }else {
            mediaPlayer = MediaPlayer.create( getApplicationContext(), uri) ;
            mediaPlayer.start();
        }
        seekBar.setMax( mediaPlayer.getDuration() );
    }

    public void displaySongInfo(int position) {
        //get songList from mainActivity
        listSongs = songArrayList;

        title.setText( listSongs.get( position ).getTitle() );
        artist.setText( listSongs.get( position ).getArtist() );
        duration.setText( getDuration( listSongs.get( position ).getDuration() ) );
        if(listSongs != null){
            playPause.setImageResource( R.drawable.ic_baseline_pause_24 );
            mUri = Uri.parse( listSongs.get( position ).getPath() );
        }
        Uri albumUri = listSongs.get( position ).getAlbumArt();
        if(albumUri != null){
            ImageAnimation( this,thumbnail,albumUri );
        }

        SharedPreferences.Editor editor = getSharedPreferences( SONG_LAST_PLAYED, MODE_PRIVATE )
                .edit();
        editor.putString( MUSIC_FILE, mUri.toString() );
        editor.putString( ARTIST_NAME, listSongs.get( position ).getArtist() );
        editor.putString( SONG_TITLE, listSongs.get( position ).getTitle() );
        editor.apply();

    }

    public void seekMediaPlayer(){
        if(mediaPlayer != null ){
            mediaPlayer.seekTo( seekBar.getProgress() * 1000);
        }
    }

    public void updateSeekBar(){
        runOnUiThread( new Runnable() {
            @Override
            public void run() {
                if(mediaPlayer != null) {
                    seekBar.setMax( mediaPlayer.getDuration() / 1000 );
                    int currentPos = mediaPlayer.getCurrentPosition() / 1000;
                    seekBar.setProgress( currentPos );
                    tvPlayed.setText( durationPlayed( currentPos ) );
                }
                handle.postDelayed( this,1000 );
            }
        } );
        mediaPlayer.setOnCompletionListener(  this );

    }

    public void playPause(){
        if(mediaPlayer != null){
            if(mediaPlayer.isPlaying()){
                    mediaPlayer.pause();
                    showNotification(R.drawable.ic_baseline_play_arrow_24);
                    playPause.setImageResource( R.drawable.ic_baseline_play_arrow_24 );
            }else{
                mediaPlayer.start();
                showNotification(R.drawable.ic_baseline_pause_24);
                playPause.setImageResource( R.drawable.ic_baseline_pause_24 );
                updateSeekBar();
            }
        }
    }

    public void nextSong(){
        if(mediaPlayer.isPlaying()){
            mediaPlayer.stop();
            mediaPlayer.release();
            if(isShuffle && !isRepeat){
                position = getRandom(listSongs.size()-1);
            }else if(!isShuffle && !isRepeat){
                position = ((position + 1) % listSongs.size());
            }
            mUri = Uri.parse( listSongs.get( position ).getPath() );
            mediaPlayer = MediaPlayer.create( getApplicationContext(), mUri );
            displaySongInfo( position );
            updateSeekBar();
            showNotification(R.drawable.ic_baseline_pause_24);
            mediaPlayer.setOnCompletionListener(  this );
            mediaPlayer.start();
        }
        else {
            mediaPlayer.stop();
            mediaPlayer.release();
            if(isShuffle && !isRepeat){
                position = getRandom(listSongs.size()-1);
            }else if(!isShuffle && !isRepeat){
                position = ((position + 1) % listSongs.size());
            }
            mUri = Uri.parse( listSongs.get( position ).getPath() );
            mediaPlayer = MediaPlayer.create( getApplicationContext(), mUri );
            displaySongInfo( position );
            updateSeekBar();
            showNotification(R.drawable.ic_baseline_play_arrow_24);
            playPause.setImageResource( R.drawable.ic_baseline_play_arrow_24 );
        }
        mediaPlayer.setOnCompletionListener(  this );
    }

    public void prevSong(){
        if(mediaPlayer.isPlaying()){
            mediaPlayer.stop();
            mediaPlayer.release();
            if(isShuffle && !isRepeat){
                position = getRandom(listSongs.size()-1);
            }else if(!isShuffle && !isRepeat){
                position = ((position - 1) < 0 ? (listSongs.size()-1) : (position - 1));
            }
            mUri = Uri.parse( listSongs.get( position ).getPath() );
            mediaPlayer = MediaPlayer.create( getApplicationContext(),mUri );
            displaySongInfo( position );
            updateSeekBar();
            mediaPlayer.start();
            showNotification(R.drawable.ic_baseline_pause_24);
            mediaPlayer.setOnCompletionListener(  this );
        }else {
            mediaPlayer.stop();
            mediaPlayer.release();
            if(isShuffle && !isRepeat){
                position = getRandom(listSongs.size()-1);
            }else if(!isShuffle && !isRepeat){
                position = ((position - 1) < 0 ? (listSongs.size()-1) : (position - 1));
            }
            mUri = Uri.parse( listSongs.get( position ).getPath() );
            mediaPlayer = MediaPlayer.create( getApplicationContext(),mUri );
            displaySongInfo( position );
            updateSeekBar();
            showNotification(R.drawable.ic_baseline_play_arrow_24);
            playPause.setImageResource( R.drawable.ic_baseline_play_arrow_24 );
        }
        mediaPlayer.setOnCompletionListener(  this );
    }

    @Override
    public void onCompletion(MediaPlayer mp) {

        nextSong();
        if(mediaPlayer != null){
            mediaPlayer = MediaPlayer.create( getApplicationContext(), mUri );
            mediaPlayer.start();
            playPause.setImageResource( R.drawable.ic_baseline_pause_24 );
            showNotification( R.drawable.ic_baseline_pause_24 );
            mediaPlayer.setOnCompletionListener(  this );
        }

    }

    public void showNotification(int playPauseBtn) {
        Intent intent = new Intent(this, AudioPlayerService.class);
        PendingIntent contentIntent = PendingIntent.getActivity( this, 0, intent, 0 );

        Intent prevIntent = new Intent(this, NotificationReceiver.class)
                .setAction( ACTION_PREV );
        PendingIntent prevPending = PendingIntent.getBroadcast( this, 0, prevIntent, PendingIntent.FLAG_UPDATE_CURRENT );

        Intent pauseIntent = new Intent(this, NotificationReceiver.class)
                .setAction( ACTION_PLAY );
        PendingIntent pausePending = PendingIntent.getBroadcast( this, 0, pauseIntent, PendingIntent.FLAG_UPDATE_CURRENT );

        Intent nextIntent = new Intent(this, NotificationReceiver.class)
                .setAction( ACTION_NEXT );
        PendingIntent nextPending = PendingIntent.getBroadcast( this, 0, nextIntent, PendingIntent.FLAG_UPDATE_CURRENT );

        Intent closeIntent = new Intent(this, NotificationReceiver.class)
                .setAction( ACTION_CLOSE );
        PendingIntent closePending = PendingIntent.getBroadcast( this, 0, closeIntent, PendingIntent.FLAG_UPDATE_CURRENT );

        byte[] picture = null;
        picture = getAlbum(mUri);
        Bitmap thumb = null;
        if(picture != null){
            thumb = BitmapFactory.decodeByteArray( picture,0,picture.length);
        }else {
            thumb = BitmapFactory.decodeResource( getResources(), R.drawable.ic_music );
        }

        Notification notification = new NotificationCompat.Builder( this, CHANNEL_ID_2 )
                .setSmallIcon( playPauseBtn )
                .setLargeIcon(thumb )
                .setContentTitle( listSongs.get( position ).getTitle() )
                .setContentText( listSongs.get( position ).getArtist() )
                .addAction( R.drawable.ic_icon_prev, "Previous", prevPending )
                .addAction( playPauseBtn, "Pause", pausePending)
                .addAction( R.drawable.ic_icon_next, "Next", nextPending )
                .addAction( R.drawable.ic_baseline_close_24, "Close",closePending )
                .setStyle( new androidx.media.app.NotificationCompat.MediaStyle()
                        .setMediaSession( mediaSessionCompat.getSessionToken() ))
                .setPriority( NotificationCompat.PRIORITY_HIGH )
                .setOnlyAlertOnce( true )
                .setVisibility( NotificationCompat.VISIBILITY_PUBLIC )
                .build();
        startForeground( 2, notification );
    }

    private String durationPlayed(int currentPos) {
        String totalOut ="";
        String totalNew ="";
        String sec = String.valueOf( currentPos % 60 );
        String mins = String.valueOf( currentPos / 60 );
        totalOut = mins + ":" + sec;
        totalNew = mins + ":" + "0" +sec;
        if(sec.length() == 1){
            return totalNew;
        }else {
            return totalOut;
        }
    }

    private String getDuration(int totalDuration){
        String duration;
        int hrs = totalDuration/(1000*60*60);
        int mins = (totalDuration%(1000*60*60))/(1000*60);
        int secs = (((totalDuration%(1000*60*60))%(1000*60*60))%(1000*60))/1000;
        if(hrs<1){
            duration = String.format( "%02d:%02d",mins,secs );
        }else{
            duration = String.format( "%1d:%02d:%02d",hrs,mins,secs );
        }
        return duration;
    }

    public static void ImageAnimation(Context context, ImageView imageView, Uri uri){
        Animation animOut = AnimationUtils.loadAnimation( context, android.R.anim.fade_out );
        Animation animIn = AnimationUtils.loadAnimation( context, android.R.anim.fade_in );
        animOut.setAnimationListener( new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }
            @Override
            public void onAnimationEnd(Animation animation) {
                if(uri != null){
                    imageView.setImageURI( uri );
                }else{
                    imageView.setImageResource( R.drawable.ic_music );
                }
                animIn.setAnimationListener( new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {
                    }
                    @Override
                    public void onAnimationEnd(Animation animation) {
                    }
                    @Override
                    public void onAnimationRepeat(Animation animation) {
                    }
                } );
                imageView.startAnimation( animIn );
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        } );
        imageView.startAnimation( animOut );
    }

    private int getRandom(int i) {
        Random random = new Random();
        return random.nextInt(i+1);
    }

    public static  byte[] getAlbum(Uri albumArt) {
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        retriever.setDataSource( songArrayList.get( position ).getPath() );
        byte [] art = retriever.getEmbeddedPicture();
        return  art;
    }


}
