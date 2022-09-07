package com.activity.audioplayer.Model;


import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;

public class Song{
    private String artist;
    private String title;
    private int duration;
    private Uri albumArt;
    private String path;

    public Song(String artist, String title, int duration, Uri albumArt, String path) {
        this.artist = artist;
        this.title = title;
        this.duration = duration;
        this.albumArt = albumArt;
        this.path = path;
    }

    protected Song(Parcel in) {
        artist = in.readString();
        title = in.readString();
        duration = in.readInt();
        albumArt = in.readParcelable( Uri.class.getClassLoader() );
    }

    public String getArtist() {
        return artist;
    }

    public String getTitle() {
        return title;
    }

    public int getDuration() {
        return duration;
    }

    public Uri getAlbumArt() {
        return albumArt;
    }

    public String getPath() {
        return path;
    }
}
