package com.activity.audioplayer.Adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.activity.audioplayer.Model.Song;
import com.activity.audioplayer.R;
import com.activity.audioplayer.View.MainActivity;
import com.activity.audioplayer.View.PlaySong;
import com.bumptech.glide.Glide;

import java.util.List;

public class SongAdapter extends RecyclerView.Adapter<SongAdapter.myViewHolder> {
    private List<Song> songList;
    boolean like;
    Context context;

    public SongAdapter(List<Song> songList, Context context){
        this.songList = songList;
        this.context = context;
    }

    @SuppressLint("NotifyDataSetChanged")
    public void updateAdapter(List<Song> songList){
        this.songList = songList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public SongAdapter.myViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from( parent.getContext() ).inflate( R.layout.song_item_layout,parent,false );
        return new myViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SongAdapter.myViewHolder holder, @SuppressLint("RecyclerView") int position) {
        holder.songTitle.setText( songList.get( position ).getTitle() );
        holder.artist.setText( songList.get( position ).getArtist() );
        holder.duration.setText( getDuration( songList.get( position ).getDuration()  ) );
        holder.like.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!like){
                    holder.like.setBackgroundTintList( ContextCompat.getColorStateList( context,R.color.button_color ) );
                    like = true;
                    Toast.makeText( context, "Added to Liked Songs", Toast.LENGTH_SHORT ).show();
                }else {
                    holder.like.setBackgroundTintList( ContextCompat.getColorStateList( context,R.color.secondary_text_color ) );
                    like = false;
                    Toast.makeText( context, "Removed from Liked Songs", Toast.LENGTH_SHORT ).show();
                }
            }
        } );
        Uri albumUri = songList.get( position ).getAlbumArt();
        if(albumUri != null){
            holder.thumbnail.setImageURI( albumUri );
        }else{
            holder.thumbnail.setImageResource( R.drawable.ic_music );
        }

        holder.itemView.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, PlaySong.class );
                intent.putExtra("data",position);
                context.startActivity( intent );
            }
        } );
    }

    @Override
    public int getItemCount() {
        return songList.size();
    }

    public static class myViewHolder extends RecyclerView.ViewHolder {
        TextView songTitle,artist,duration;
        ImageView like,thumbnail;

        public myViewHolder(@NonNull View itemView) {
            super( itemView );

            songTitle = itemView.findViewById( R.id.songTitle );
            artist = itemView.findViewById( R.id.artist );
            duration = itemView.findViewById( R.id.duration );
            like = itemView.findViewById( R.id.likeBtn );
            thumbnail = itemView.findViewById( R.id.thumbnail );
        }
    }

    @SuppressLint("DefaultLocale")
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

}
