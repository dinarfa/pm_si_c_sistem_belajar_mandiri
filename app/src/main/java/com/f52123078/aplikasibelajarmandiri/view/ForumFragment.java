package com.f52123078.aplikasibelajarmandiri.view;

import android.content.Intent;
import android.content.res.ColorStateList; // Tambahkan ini
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat; // Tambahkan ini
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.f52123078.aplikasibelajarmandiri.R; // Pastikan R terimport
import com.f52123078.aplikasibelajarmandiri.databinding.FragmentForumBinding;
import com.f52123078.aplikasibelajarmandiri.model.ForumPost;
import com.f52123078.aplikasibelajarmandiri.model.Prodi;
import com.f52123078.aplikasibelajarmandiri.controller.AddPostActivity;
import com.f52123078.aplikasibelajarmandiri.controller.ForumAdapter;
import com.google.android.material.chip.Chip;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import java.util.ArrayList;
import java.util.List;

public class ForumFragment extends Fragment {

    private FragmentForumBinding binding;
    private FirebaseFirestore db;
    private ForumAdapter adapter;
    private List<ForumPost> allPosts = new ArrayList<>();
    private ListenerRegistration firestoreListener;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentForumBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        db = FirebaseFirestore.getInstance();
        setupRecyclerView();
        loadProdiChips();
        loadPostsRealtime(null);

        // 1. Listener Search
        binding.etSearchForum.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterListLocal(s.toString());
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        // 2. Listener Filter Chip
        binding.chipGroupForum.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.isEmpty()) {
                // Jika tidak ada yang dipilih, reload semua (opsional, tapi biasanya singleSelection perlu 1 yang aktif)
                loadPostsRealtime(null);
            } else {
                int id = checkedIds.get(0);
                Chip chip = group.findViewById(id);
                if (chip != null) {
                    // Cek apakah chip "Semua" atau chip Prodi
                    if (chip.getId() == binding.chipAllForum.getId()) {
                        loadPostsRealtime(null); // Load Semua
                    } else {
                        String prodiName = (String) chip.getTag();
                        loadPostsRealtime(prodiName);
                    }
                }
            }
        });

        binding.fabAddPost.setOnClickListener(v -> startActivity(new Intent(getActivity(), AddPostActivity.class)));
    }

    private void setupRecyclerView() {
        adapter = new ForumAdapter(new ArrayList<>());
        binding.rvForum.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.rvForum.setAdapter(adapter);
    }

    // --- BAGIAN YANG DIUBAH ADA DI SINI ---
    private void loadProdiChips() {
        db.collection("prodi").orderBy("name").get().addOnSuccessListener(snapshot -> {
            for (Prodi p : snapshot.toObjects(Prodi.class)) {
                Chip chip = new Chip(requireContext());
                chip.setText(p.getName());
                chip.setTag(p.getName());
                chip.setCheckable(true);

                // --- UPDATE STYLE CHIP ---
                // Mengambil ColorStateList dari file XML yang kita buat tadi
                ColorStateList bgStateList = ContextCompat.getColorStateList(requireContext(), R.color.state_chip_bg);
                ColorStateList textStateList = ContextCompat.getColorStateList(requireContext(), R.color.state_chip_text);

                // Set Background Logic
                chip.setChipBackgroundColor(bgStateList);

                // Set Text Color Logic
                chip.setTextColor(textStateList);

                // Set Stroke (Garis pinggir)
                chip.setChipStrokeColorResource(android.R.color.darker_gray);
                chip.setChipStrokeWidth(1f); // 1dp stroke width

                // Tambahkan ke Group
                binding.chipGroupForum.addView(chip);
            }
        });
    }

    private void loadPostsRealtime(String prodiFilter) {
        binding.progressBarForum.setVisibility(View.VISIBLE);
        if (firestoreListener != null) firestoreListener.remove();

        Query query = db.collection("forum_posts");

        if (prodiFilter != null && !prodiFilter.equals("Semua")) {
            query = query.whereEqualTo("prodiName", prodiFilter);
        }

        query = query.orderBy("timestamp", Query.Direction.DESCENDING);

        firestoreListener = query.addSnapshotListener((value, error) -> {
            binding.progressBarForum.setVisibility(View.GONE);
            if (error != null) {
                Log.e("ForumFragment", "Listen failed.", error);
                // Toast.makeText(getContext(), "Gagal: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                return;
            }

            if (value != null) {
                allPosts.clear();
                for (DocumentSnapshot doc : value.getDocuments()) {
                    ForumPost post = doc.toObject(ForumPost.class);
                    if (post != null) {
                        post.setPostId(doc.getId());
                        allPosts.add(post);
                    }
                }
                filterListLocal(binding.etSearchForum.getText().toString());
            }
        });
    }

    private void filterListLocal(String query) {
        String lower = query.toLowerCase().trim();
        List<ForumPost> filtered = new ArrayList<>();

        for (ForumPost p : allPosts) {
            boolean matchText = p.getText() != null && p.getText().toLowerCase().contains(lower);
            boolean matchMk = p.getMkName() != null && p.getMkName().toLowerCase().contains(lower);

            if (matchText || matchMk) {
                filtered.add(p);
            }
        }

        adapter.updateList(filtered);
        binding.tvEmptyForum.setVisibility(filtered.isEmpty() ? View.VISIBLE : View.GONE);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (firestoreListener != null) firestoreListener.remove();
        binding = null;
    }
}