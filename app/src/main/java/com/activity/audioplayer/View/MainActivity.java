package com.activity.audioplayer.View;

import static com.activity.audioplayer.AudioPlayerService.MUSIC_FILE;
import static com.activity.audioplayer.AudioPlayerService.SONG_LAST_PLAYED;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Dialog;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;

import com.activity.audioplayer.R;
import com.activity.audioplayer.Model.Song;
import com.activity.audioplayer.Adapter.SongAdapter;
import com.activity.audioplayer.ViewModel.SongViewModel;
import com.activity.audioplayer.databinding.ActivityMainBinding;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    Dialog dialog;
    public static List<Song> songArrayList = new ArrayList<>();
    SongViewModel songViewModel;
    @SuppressLint("StaticFieldLeak")
    private static Context context;
    SongAdapter adapter;
    public static final String SONG_LAST_PLAYED = "LAST_PLAYED";
    public static final String MUSIC_FILE = "MUSIC_URI";
    public static final String ARTIST_NAME = "ARTIST NAME";
    public static final String SONG_TITLE = "SONG TITLE";
    public static boolean SHOW_BOTTOM_PLAYER = false;
    public static String PATH_FRAGMENT = null;
    public static String SONG_TITLE_FRAGMENT = null;
    public static String ARTIST_NAME_FRAGMENT = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate( savedInstanceState );
        binding = ActivityMainBinding.inflate( getLayoutInflater() );
        setContentView( binding.getRoot() );
        getWindow().setFlags( WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN );
        //use in getContentResolver in SongRepository
        context = getApplicationContext();

        runTimePermission();

    }

    private void runTimePermission() {
        Dexter.withContext( this ).withPermission( Manifest.permission.READ_EXTERNAL_STORAGE )
                .withListener( new PermissionListener() {
                    @Override
                    public void onPermissionGranted(PermissionGrantedResponse permissionGrantedResponse) {
                        displaySongs( );
                    }

                    @Override
                    public void onPermissionDenied(PermissionDeniedResponse permissionDeniedResponse) {
                        dialog = new Dialog( MainActivity.this );
                        dialog.setContentView( R.layout.permission_layout );
                        dialog.getWindow().setBackgroundDrawable( new ColorDrawable( Color.TRANSPARENT) );

                        Button allowBtn = dialog.findViewById( R.id.allowBtn );
                        allowBtn.setOnClickListener( new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                runTimePermission();
                                dialog.dismiss();
                            }
                        } );
                        dialog.show();
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(PermissionRequest permissionRequest, PermissionToken permissionToken) {
                        permissionToken.continuePermissionRequest();
                    }
                } ).check();
    }

    private void displaySongs() {
        binding.recyclerView.setLayoutManager( new LinearLayoutManager( this ) );
        adapter = new SongAdapter( songArrayList,this );
        binding.recyclerView.setAdapter( adapter );
        songViewModel = new ViewModelProvider( this ).get( SongViewModel.class );
        songViewModel.getSongList().observe( this, new Observer<List<Song>>() {
            @Override
            public void onChanged(List<Song> songList) {
                songArrayList = songList;
                adapter.updateAdapter( songList );
            }
        } );
    }

    public static Context getContextOfApplication() {
        return context;
    }

    @Override
    protected void onResume() {
        super.onResume();
        SharedPreferences preferences = getSharedPreferences( SONG_LAST_PLAYED, MODE_PRIVATE );
        String path = preferences.getString( MUSIC_FILE,null );
        String title = preferences.getString( SONG_TITLE,null );
        String artist = preferences.getString( ARTIST_NAME,null );
        if(path != null){
            SHOW_BOTTOM_PLAYER = true;
            PATH_FRAGMENT = path;
            SONG_TITLE_FRAGMENT = title;
            ARTIST_NAME_FRAGMENT = artist;
        }else{
            SHOW_BOTTOM_PLAYER = false;
            PATH_FRAGMENT = null;
            SONG_TITLE_FRAGMENT = null;
            ARTIST_NAME_FRAGMENT = null;
        }
    }
}