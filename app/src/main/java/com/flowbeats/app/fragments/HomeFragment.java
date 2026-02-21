package com.flowbeats.app.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;

import com.flowbeats.app.R;

import android.content.pm.PackageManager;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.flowbeats.app.adapters.SongAdapter;
import com.flowbeats.app.models.Song;
import com.flowbeats.app.player.MusicPlayer;
import com.flowbeats.app.viewmodels.HomeViewModel;
import java.util.ArrayList;

public class HomeFragment extends Fragment {
    private RecyclerView recyclerView;
    private SongAdapter adapter;
    private HomeViewModel homeViewModel;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        adapter = new SongAdapter(getContext(), position -> {
            // Play the selected song by position
            MusicPlayer.getInstance(getContext()).playSong(position);
        });
        recyclerView.setAdapter(adapter);

        homeViewModel = new ViewModelProvider(this).get(HomeViewModel.class);
        homeViewModel.getSongs().observe(getViewLifecycleOwner(), songs -> {
            if (songs != null) {
                adapter.setSongs(songs);
                // Also update the playlist in MusicPlayer so next/prev works
                MusicPlayer.getInstance(getContext()).setPlaylist(songs);
            }
        });

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        checkPermissionsAndLoad();
    }

    private void checkPermissionsAndLoad() {
        if (getContext() != null) {
            boolean permissionGranted = false;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                permissionGranted = ContextCompat.checkSelfPermission(getContext(),
                        android.Manifest.permission.READ_MEDIA_AUDIO) == PackageManager.PERMISSION_GRANTED;
            } else {
                permissionGranted = ContextCompat.checkSelfPermission(getContext(),
                        android.Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
            }

            if (permissionGranted) {
                homeViewModel.loadSongs();
            }
        }
    }
}
