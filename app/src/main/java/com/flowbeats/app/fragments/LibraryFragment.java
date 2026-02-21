package com.flowbeats.app.fragments;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.flowbeats.app.R;
import com.flowbeats.app.activities.PlaylistActivity;
import com.flowbeats.app.adapters.PlaylistAdapter;
import com.flowbeats.app.viewmodels.MusicViewModel;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.Collections;

public class LibraryFragment extends Fragment {
    private RecyclerView recyclerView;
    private FloatingActionButton fabAddPlaylist;
    private PlaylistAdapter adapter;
    private MusicViewModel viewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_library, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState); // fixed line number

        android.widget.Toast.makeText(getContext(), "Library Fragment Loaded", android.widget.Toast.LENGTH_SHORT)
                .show();

        recyclerView = view.findViewById(R.id.rvLibrarySongs);
        fabAddPlaylist = view.findViewById(R.id.fabAdd);

        viewModel = new ViewModelProvider(requireActivity()).get(MusicViewModel.class);

        setupRecyclerView();
        setupFab();
        observePlaylists();
    }

    private void setupRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new PlaylistAdapter(playlist -> {
            Intent intent = new Intent(getContext(), PlaylistActivity.class);
            intent.putExtra("playlist_id", playlist.getId());
            intent.putExtra("playlist_name", playlist.getName());
            startActivity(intent);
        });
        recyclerView.setAdapter(adapter);
    }

    private void setupFab() {
        fabAddPlaylist.setOnClickListener(v -> showCreatePlaylistDialog());
    }

    private void observePlaylists() {
        viewModel.getPlaylistsWithCounts().observe(getViewLifecycleOwner(), playlists -> {
            if (playlists != null) {
                adapter.setPlaylists(playlists);
            } else {
                adapter.setPlaylists(Collections.emptyList());
            }
        });
    }

    private void showCreatePlaylistDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("New Playlist");

        final EditText input = new EditText(getContext());
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(input);

        builder.setPositiveButton("Create", (dialog, which) -> {
            String playlistName = input.getText().toString();
            if (!playlistName.isEmpty()) {
                viewModel.createPlaylist(playlistName);
            }
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        builder.show();
    }
}
