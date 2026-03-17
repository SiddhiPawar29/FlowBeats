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
import com.flowbeats.app.player.MusicPlayer;
import com.flowbeats.app.viewmodels.HomeViewModel;
import com.flowbeats.app.viewmodels.MusicViewModel;
import com.flowbeats.app.adapters.PlaylistAdapter;
import com.flowbeats.app.activities.PlaylistActivity;
import com.flowbeats.app.activities.MainActivity;
import androidx.appcompat.widget.PopupMenu;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Calendar;
import android.content.Context;
import android.content.SharedPreferences;

public class HomeFragment extends Fragment {
    private RecyclerView recyclerView;
    private RecyclerView rvPlaylists;
    private SongAdapter adapter;
    private PlaylistAdapter playlistAdapter;
    private HomeViewModel homeViewModel;
    private MusicViewModel musicViewModel;

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

        rvPlaylists = view.findViewById(R.id.rvPlaylists);
        rvPlaylists.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        playlistAdapter = new PlaylistAdapter(false, playlist -> {
            android.content.Intent intent = new android.content.Intent(getContext(), PlaylistActivity.class);
            intent.putExtra("playlist_id", playlist.getId());
            intent.putExtra("playlist_name", playlist.getName());
            startActivity(intent);
        }, playlist -> {
            new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setTitle("Delete Playlist")
                .setMessage("Are you sure you want to delete '" + playlist.getName() + "'?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    musicViewModel.deletePlaylist(playlist);
                })
                .setNegativeButton("Cancel", null)
                .show();
        });
        rvPlaylists.setAdapter(playlistAdapter);

        musicViewModel = new ViewModelProvider(requireActivity()).get(MusicViewModel.class);
        musicViewModel.getPlaylistsWithCounts().observe(getViewLifecycleOwner(), playlists -> {
            if (playlists != null) {
                playlistAdapter.setPlaylists(playlists);
            } else {
                playlistAdapter.setPlaylists(Collections.emptyList());
            }
        });

        android.widget.TextView tvSeeAll = view.findViewById(R.id.tvSeeAll);
        if (tvSeeAll != null) {
            tvSeeAll.setOnClickListener(v -> {
                if (getActivity() instanceof MainActivity) {
                    ((MainActivity) getActivity()).switchToLibraryTab();
                }
            });
        }

        // --- Dynamic Greeting ---
        android.widget.TextView tvGreeting = view.findViewById(R.id.tvGreeting);
        if (tvGreeting != null) {
            Calendar c = Calendar.getInstance();
            int timeOfDay = c.get(Calendar.HOUR_OF_DAY);

            if (timeOfDay >= 5 && timeOfDay < 12) {
                tvGreeting.setText("Good Morning");
            } else if (timeOfDay >= 12 && timeOfDay < 17) {
                tvGreeting.setText("Good Afternoon");
            } else {
                tvGreeting.setText("Good Evening");
            }
        }

        // --- Profile Sign Out ---
        View cardProfile = view.findViewById(R.id.cardProfile);
        if (cardProfile != null) {
            cardProfile.setOnClickListener(v -> {
                PopupMenu popupMenu = new PopupMenu(requireContext(), cardProfile);
                popupMenu.getMenu().add("Sign Out");
                popupMenu.setOnMenuItemClickListener(item -> {
                    if ("Sign Out".equals(item.getTitle().toString())) {
                        // Clear prefs if any
                        SharedPreferences prefs = requireContext().getSharedPreferences("user_session",
                                Context.MODE_PRIVATE);
                        prefs.edit().clear().apply();

                        // Navigate to Login Activity
                        android.content.Intent intent = new android.content.Intent(getContext(),
                                com.flowbeats.app.activities.LoginActivity.class);
                        // Prevent returning back to MainActivity on back press
                        intent.setFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK
                                | android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        if (getActivity() != null) {
                            getActivity().finish();
                        }
                        return true;
                    }
                    return false;
                });
                popupMenu.show();
            });
        }

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
