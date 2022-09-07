package com.activity.audioplayer.View;


import static android.content.Context.MODE_PRIVATE;
import static com.activity.audioplayer.AudioPlayerService.mediaPlayer;
import static com.activity.audioplayer.View.MainActivity.ARTIST_NAME_FRAGMENT;
import static com.activity.audioplayer.View.MainActivity.PATH_FRAGMENT;
import static com.activity.audioplayer.View.MainActivity.SHOW_BOTTOM_PLAYER;
import static com.activity.audioplayer.View.MainActivity.SONG_TITLE_FRAGMENT;
import static com.activity.audioplayer.View.MainActivity.songArrayList;
import static com.activity.audioplayer.View.PlaySong.listSongs;
import static com.activity.audioplayer.View.PlaySong.position;
import static com.activity.audioplayer.View.PlaySong.thumbnail;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.os.IBinder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import com.activity.audioplayer.AudioPlayerService;
import com.activity.audioplayer.R;
import com.activity.audioplayer.databinding.FragmentBottomBinding;
import com.google.android.material.floatingactionbutton.FloatingActionButton;


public class BottomFragment extends Fragment implements ServiceConnection {

    ImageView nextBot,prevBot,thumbnailBot;
    FloatingActionButton playPauseBot;
    View view;
    TextView title,artist;
    boolean mBound;
    AudioPlayerService audioPlayerService;
    public static final String SONG_LAST_PLAYED = "LAST_PLAYED";
    public static final String MUSIC_FILE = "MUSIC_URI";
    public static final String ARTIST_NAME = "ARTIST NAME";
    public static final String SONG_TITLE = "SONG TITLE";


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate( R.layout.fragment_bottom, container,false );

        nextBot = view.findViewById( R.id.nextBtn );
        prevBot = view.findViewById( R.id.prevBtn );
        thumbnailBot = view.findViewById( R.id.thumbnailBot );
        nextBot = view.findViewById( R.id.nextBtn );
        playPauseBot = view.findViewById( R.id.bottom_playBtn );
        title = view.findViewById( R.id.title_bottom );
        artist = view.findViewById( R.id.artist_bottom );

        nextBot.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(audioPlayerService != null){
                    audioPlayerService.nextSong();
                    if(getActivity() != null){
                        SharedPreferences.Editor editor = getActivity().getSharedPreferences( SONG_LAST_PLAYED, MODE_PRIVATE )
                                .edit();
                        editor.putString( MUSIC_FILE, AudioPlayerService.listSongs.get( position ).getPath() );
                        editor.putString( ARTIST_NAME, AudioPlayerService.listSongs.get( position ).getArtist() );
                        editor.putString( SONG_TITLE, AudioPlayerService.listSongs.get( position ).getTitle() );
                        editor.apply();
                        SharedPreferences preferences = getActivity().getSharedPreferences( SONG_LAST_PLAYED, MODE_PRIVATE );
                        String path = preferences.getString( MUSIC_FILE,null );
                        String titleName = preferences.getString( SONG_TITLE,null );
                        String artistName = preferences.getString( ARTIST_NAME,null );
                        if(path != null){
                            SHOW_BOTTOM_PLAYER = true;
                            PATH_FRAGMENT = path;
                            SONG_TITLE_FRAGMENT = titleName;
                            ARTIST_NAME_FRAGMENT = artistName;
                        }else{
                            SHOW_BOTTOM_PLAYER = false;
                            PATH_FRAGMENT = null;
                            SONG_TITLE_FRAGMENT = null;
                            ARTIST_NAME_FRAGMENT = null;
                        }
                        if(SHOW_BOTTOM_PLAYER){
                            if(PATH_FRAGMENT != null){
                                byte [] art = getAlbum( PATH_FRAGMENT );
                                if (art != null){
                                    Bitmap albumArt = BitmapFactory.decodeByteArray( art, 0, art.length );
                                    thumbnailBot.setImageBitmap( albumArt );
                                }else{
                                    Bitmap albumArt = BitmapFactory.decodeResource( getResources(), R.drawable.ic_music );
                                    thumbnailBot.setImageBitmap( albumArt );
                                }
                                title.setText( SONG_TITLE_FRAGMENT );
                                artist.setText( ARTIST_NAME_FRAGMENT );
                            }

                        }
                    }
                }
            }
        } );

        playPauseBot.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(audioPlayerService != null){
                    audioPlayerService.playPause();
                    if(mediaPlayer.isPlaying()){
                        playPauseBot.setImageResource( R.drawable.ic_baseline_pause_24 );
                    }else{
                        playPauseBot.setImageResource( R.drawable.ic_baseline_play_arrow_24 );
                    }
                }
            }
        } );

        prevBot.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(audioPlayerService != null){
                    audioPlayerService.prevSong();
                    if(getActivity() != null){
                        SharedPreferences.Editor editor = getActivity().getSharedPreferences( SONG_LAST_PLAYED, MODE_PRIVATE )
                                .edit();
                        editor.putString( MUSIC_FILE, AudioPlayerService.listSongs.get( position ).getPath() );
                        editor.putString( ARTIST_NAME, AudioPlayerService.listSongs.get( position ).getArtist() );
                        editor.putString( SONG_TITLE, AudioPlayerService.listSongs.get( position ).getTitle() );
                        editor.apply();
                        SharedPreferences preferences = getActivity().getSharedPreferences( SONG_LAST_PLAYED, MODE_PRIVATE );
                        String path = preferences.getString( MUSIC_FILE,null );
                        String titleName = preferences.getString( SONG_TITLE,null );
                        String artistName = preferences.getString( ARTIST_NAME,null );
                        if(path != null){
                            SHOW_BOTTOM_PLAYER = true;
                            PATH_FRAGMENT = path;
                            SONG_TITLE_FRAGMENT = titleName;
                            ARTIST_NAME_FRAGMENT = artistName;
                        }else{
                            SHOW_BOTTOM_PLAYER = false;
                            PATH_FRAGMENT = null;
                            SONG_TITLE_FRAGMENT = null;
                            ARTIST_NAME_FRAGMENT = null;
                        }
                        if(SHOW_BOTTOM_PLAYER){
                            if(PATH_FRAGMENT != null){
                                byte [] art = getAlbum( PATH_FRAGMENT );
                                if (art != null){
                                    Bitmap albumArt = BitmapFactory.decodeByteArray( art, 0, art.length );
                                    thumbnailBot.setImageBitmap( albumArt );
                                }else{
                                    Bitmap albumArt = BitmapFactory.decodeResource( getResources(), R.drawable.ic_music );
                                    thumbnailBot.setImageBitmap( albumArt );
                                }
                                title.setText( SONG_TITLE_FRAGMENT );
                                artist.setText( ARTIST_NAME_FRAGMENT );
                            }

                        }
                    }
                }
            }
        } );

        return view;


    }

    @Override
    public void onResume() {
        super.onResume();
        if(SHOW_BOTTOM_PLAYER){
            if(PATH_FRAGMENT != null){
                byte [] art = getAlbum( PATH_FRAGMENT );
                if (art != null){
                    Bitmap albumArt = BitmapFactory.decodeByteArray( art, 0, art.length );
                    thumbnailBot.setImageBitmap( albumArt );
                }else{
                    Bitmap albumArt = BitmapFactory.decodeResource( getResources(), R.drawable.ic_music );
                    thumbnailBot.setImageBitmap( albumArt );
                }
                title.setText( SONG_TITLE_FRAGMENT );
                artist.setText( ARTIST_NAME_FRAGMENT );
                if(mediaPlayer.isPlaying()){
                    playPauseBot.setImageResource( R.drawable.ic_baseline_pause_24 );
                }else{
                    playPauseBot.setImageResource( R.drawable.ic_baseline_play_arrow_24 );
                }

                Intent intent = new Intent(getContext(), AudioPlayerService.class);
                if(getContext() != null){
                    getContext().bindService( intent, this, Context.BIND_AUTO_CREATE );
                }
            }

        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if(getContext() != null){
            if(mBound){
                getContext().unbindService( this );
                mBound = false;
            }
        }
    }

    public static  byte[] getAlbum(String uri) {
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        retriever.setDataSource( uri );
        byte [] art = retriever.getEmbeddedPicture();
        return  art;
    }

    @Override
    public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
        AudioPlayerService.LocalBinder binder = (AudioPlayerService.LocalBinder) iBinder;
        audioPlayerService = binder.getService();
        mBound = true;
    }

    @Override
    public void onServiceDisconnected(ComponentName componentName) {
        audioPlayerService = null;
    }
}