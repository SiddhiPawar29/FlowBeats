package com.flowbeats.app.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.flowbeats.app.R;
import com.flowbeats.app.models.Song;

import java.util.ArrayList;
import java.util.List;

public class SongAdapter extends RecyclerView.Adapter<SongAdapter.SongViewHolder> {
    private List<Song> songList = new ArrayList<>();
    private Context context;
    private OnSongClickListener listener;

    public interface OnSongClickListener {
        void onSongClick(int position);
    }

    public SongAdapter(Context context, OnSongClickListener listener) {
        this.context = context;
        this.listener = listener;
    }

    public void setSongs(List<Song> songs) {
        this.songList = songs;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public SongViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_song, parent, false);
        return new SongViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SongViewHolder holder, int position) {
        Song song = songList.get(position);
        holder.tvTitle.setText(song.getTitle());
        holder.tvArtist.setText(song.getArtist());
        holder.tvDuration.setText(song.getFormattedDuration());

        if (song.getAlbumArt() != null) {
            Glide.with(context)
                    .load(song.getAlbumArt())
                    .placeholder(android.R.drawable.ic_media_play) // Use a default system icon for testing
                    .error(android.R.drawable.ic_media_play)
                    .into(holder.ivAlbumArt);
        } else {
            holder.ivAlbumArt.setImageResource(android.R.drawable.ic_media_play);
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onSongClick(holder.getAdapterPosition());
            }
        });
    }

    @Override
    public int getItemCount() {
        return songList.size();
    }

    static class SongViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvArtist, tvDuration;
        ImageView ivAlbumArt;

        public SongViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvSongName);
            tvArtist = itemView.findViewById(R.id.tvSongArtist);
            tvDuration = itemView.findViewById(R.id.tvDuration);
            ivAlbumArt = itemView.findViewById(R.id.albumArt);
        }
    }
}
