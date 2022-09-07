package com.activity.audioplayer.Repository;

import android.annotation.SuppressLint;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.util.Log;

import com.activity.audioplayer.Model.Song;
import com.activity.audioplayer.View.MainActivity;

import java.util.ArrayList;
import java.util.List;

public class SongRepository {

    OnSongAdded onSongAdded;
    Uri songUri;
    List<Song> songList = new ArrayList<>();


    public SongRepository( OnSongAdded onSongAdded){
        this.onSongAdded = onSongAdded;

    }

    public void fetchSongs(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            songUri = MediaStore.Audio.Media.getContentUri( MediaStore.VOLUME_EXTERNAL );
        }else{
            songUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        }

        //data we need from the MediaStore
        String[] projection = {
                MediaStore.Audio.Media.ARTIST,
                MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.DURATION,
                MediaStore.Audio.Media.ALBUM_ID,
                MediaStore.Audio.Media.DATA
        };

        //sort
        String sortOrder = MediaStore.Audio.Media.TITLE + " ASC";

        Context context = MainActivity.getContextOfApplication();
        //Querying
        Cursor cursor = context.getContentResolver().query( songUri, projection, null, null, sortOrder);


        //cache the cursor indices
        int titleColumn = cursor.getColumnIndexOrThrow( MediaStore.Audio.Media.TITLE );
        int artistColumn = cursor.getColumnIndexOrThrow( MediaStore.Audio.Media.ARTIST );
        int durationColumn = cursor.getColumnIndexOrThrow( MediaStore.Audio.Media.DURATION );
        int albumIdColumn = cursor.getColumnIndexOrThrow( MediaStore.Audio.Media.ALBUM_ID );
        int dataColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA);




        //getting the values
        while(cursor.moveToNext()) {
            //get value of columns for audio file
            String artist = cursor.getString( artistColumn );
            int duration = cursor.getInt( durationColumn );
            String nameSong = cursor.getString( titleColumn );
            long albumId = cursor.getLong(albumIdColumn);
            String path = cursor.getString(dataColumn);

            //song thumbnail
            Uri albumUri = ContentUris.withAppendedId( Uri.parse( "content://media/external/audio/albumart" ), albumId );

            //adding to the songList
            songList.add( new Song( artist, nameSong, duration,albumUri,path) );
            onSongAdded.songDataAdded( songList );

        }
    }


    public interface OnSongAdded{
        void songDataAdded(List<Song> songList);
    }
}
