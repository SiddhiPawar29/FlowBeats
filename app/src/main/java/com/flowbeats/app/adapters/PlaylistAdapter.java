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
    private OnPlaylistLongClickListener longClickListener;
    private List<com.flowbeats.app.models.PlaylistWithSongCount> playlists = new ArrayList<>();
    private boolean isLibraryView = false;

    public interface OnPlaylistClickListener {
        void onPlaylistClick(com.flowbeats.app.models.Playlist playlist);
    }

    public interface OnPlaylistLongClickListener {
        void onPlaylistLongClick(com.flowbeats.app.models.Playlist playlist);
    }

    public PlaylistAdapter(boolean isLibraryView, OnPlaylistClickListener listener, OnPlaylistLongClickListener longClickListener) {
        this.isLibraryView = isLibraryView;
        this.listener = listener;
        this.longClickListener = longClickListener;
    }

    public void setPlaylists(List<com.flowbeats.app.models.PlaylistWithSongCount> playlists) {
        this.playlists = playlists;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public PlaylistViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        int layoutId = isLibraryView ? com.flowbeats.app.R.layout.item_playlist_library : com.flowbeats.app.R.layout.item_playlist_card;
        View view = LayoutInflater.from(parent.getContext())
                .inflate(layoutId, parent, false);
        return new PlaylistViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PlaylistViewHolder holder, int position) {
        com.flowbeats.app.models.PlaylistWithSongCount item = playlists.get(position);
        holder.bind(item, listener, longClickListener);
    }

    @Override
    public int getItemCount() {
        return playlists.size();
    }

    class PlaylistViewHolder extends RecyclerView.ViewHolder {
        private TextView tvName, tvCount;

        PlaylistViewHolder(@NonNull View itemView) {
            super(itemView);
            if (isLibraryView) {
                tvName = itemView.findViewById(com.flowbeats.app.R.id.tvPlaylistNameLib);
                tvCount = itemView.findViewById(com.flowbeats.app.R.id.tvSongCountLib);
            } else {
                tvName = itemView.findViewById(com.flowbeats.app.R.id.tvPlaylistName);
                tvCount = itemView.findViewById(com.flowbeats.app.R.id.tvSongCount);
            }
        }

        void bind(com.flowbeats.app.models.PlaylistWithSongCount item, OnPlaylistClickListener listener, OnPlaylistLongClickListener longClickListener) {
            if (tvName != null) tvName.setText(item.playlist.getName());
            if (tvCount != null) tvCount.setText(item.songCount + " songs");
            
            itemView.setOnClickListener(v -> {
                if (listener != null) listener.onPlaylistClick(item.playlist);
            });

            itemView.setOnLongClickListener(v -> {
                if (longClickListener != null) {
                    longClickListener.onPlaylistLongClick(item.playlist);
                    return true;
                }
                return false;
            });
        }
    }
}
