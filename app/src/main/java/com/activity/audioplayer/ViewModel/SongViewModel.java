package com.activity.audioplayer.ViewModel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.activity.audioplayer.Model.Song;
import com.activity.audioplayer.Repository.SongRepository;

import java.util.List;

public class SongViewModel extends ViewModel implements SongRepository.OnSongAdded {

    MutableLiveData<List<Song>> songLiveData = new MutableLiveData<>();
    SongRepository songRepository = new SongRepository( this );

    public SongViewModel(){
        songRepository.fetchSongs();
    }

    public LiveData<List<Song>> getSongList(){
        return songLiveData;
    }

    @Override
    public void songDataAdded(List<Song> songList) {
        songLiveData.postValue( songList );
    }
}
