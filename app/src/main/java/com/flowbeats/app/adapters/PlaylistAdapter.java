package com.flowbeats.app.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.flowbeats.app.models.Playlist;

import java.util.ArrayList;
import java.util.List;

public class PlaylistAdapter extends RecyclerView.Adapter<PlaylistAdapter.PlaylistViewHolder> {
    private OnPlaylistClickListener listener;
    private List<com.flowbeats.app.models.PlaylistWithSongCount> playlists = new ArrayList<>();

    public interface OnPlaylistClickListener {
        void onPlaylistClick(com.flowbeats.app.models.Playlist playlist);
    }

    public PlaylistAdapter(OnPlaylistClickListener listener) {
        this.listener = listener;
    }

    public void setPlaylists(List<com.flowbeats.app.models.PlaylistWithSongCount> playlists) {
        this.playlists = playlists;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public PlaylistViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(android.R.layout.simple_list_item_2, parent, false);
        return new PlaylistViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PlaylistViewHolder holder, int position) {
        com.flowbeats.app.models.PlaylistWithSongCount item = playlists.get(position);
        holder.bind(item, listener);
    }

    @Override
    public int getItemCount() {
        return playlists.size();
    }

    class PlaylistViewHolder extends RecyclerView.ViewHolder {
        private TextView tvName, tvCount;

        PlaylistViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(android.R.id.text1);
            tvCount = itemView.findViewById(android.R.id.text2);
        }

        void bind(com.flowbeats.app.models.PlaylistWithSongCount item, OnPlaylistClickListener listener) {
            tvName.setText(item.playlist.getName());
            tvCount.setText(item.songCount + " songs");
            itemView.setOnClickListener(v -> listener.onPlaylistClick(item.playlist));
        }
    }
}
