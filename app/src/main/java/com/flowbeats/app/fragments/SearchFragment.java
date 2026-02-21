package com.flowbeats.app.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.flowbeats.app.R;
import com.flowbeats.app.adapters.SongAdapter;
import com.flowbeats.app.models.Song;
import com.flowbeats.app.player.MusicPlayer;
import com.flowbeats.app.viewmodels.MusicViewModel;

import java.util.Collections;

public class SearchFragment extends Fragment {
    private RecyclerView recyclerView;
    private SongAdapter adapter;
    private MusicViewModel viewModel;
    private android.widget.EditText etSearch;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_search, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        recyclerView = view.findViewById(R.id.rvSearchResults);
        etSearch = view.findViewById(R.id.etSearch);

        setupRecyclerView();
        setupViewModel();
        setupSearchInput();
    }

    private void setupRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new SongAdapter(getContext(), position -> {
            if (viewModel.getSearchResults().getValue() != null) {
                MusicPlayer player = MusicPlayer.getInstance(getContext());
                player.setPlaylist(viewModel.getSearchResults().getValue());
                player.playSong(position);
            }
        });
        recyclerView.setAdapter(adapter);
    }

    private void setupViewModel() {
        viewModel = new ViewModelProvider(requireActivity()).get(MusicViewModel.class);

        viewModel.getSearchResults().observe(getViewLifecycleOwner(), songs -> {
            if (songs != null) {
                adapter.setSongs(songs);
            } else {
                adapter.setSongs(Collections.emptyList());
            }
        });
    }

    private void setupSearchInput() {
        etSearch.addTextChangedListener(new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                viewModel.search(s.toString());
            }

            @Override
            public void afterTextChanged(android.text.Editable s) {
            }
        });

        etSearch.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == android.view.inputmethod.EditorInfo.IME_ACTION_SEARCH) {
                viewModel.search(etSearch.getText().toString());
                android.view.inputmethod.InputMethodManager imm = (android.view.inputmethod.InputMethodManager) requireContext()
                        .getSystemService(android.content.Context.INPUT_METHOD_SERVICE);
                if (imm != null) {
                    imm.hideSoftInputFromWindow(etSearch.getWindowToken(), 0);
                }
                etSearch.clearFocus();
                return true;
            }
            return false;
        });
    }
}
