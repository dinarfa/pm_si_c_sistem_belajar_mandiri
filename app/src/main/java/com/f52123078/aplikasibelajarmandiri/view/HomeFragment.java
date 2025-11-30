package com.f52123078.aplikasibelajarmandiri.view;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.f52123078.aplikasibelajarmandiri.databinding.FragmentHomeBinding;
import com.f52123078.aplikasibelajarmandiri.model.HomeModel;
import com.f52123078.aplikasibelajarmandiri.model.Resource;
import com.f52123078.aplikasibelajarmandiri.controller.MainActivity;
import com.f52123078.aplikasibelajarmandiri.NotificationActivity;
import com.f52123078.aplikasibelajarmandiri.controller.ResourceAdapter;
import com.f52123078.aplikasibelajarmandiri.controller.ResourceBrowseActivity;
import com.f52123078.aplikasibelajarmandiri.controller.WebViewActivity;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class HomeFragment extends Fragment implements HomeModel.HomeDataListener {

    private static final String TAG = "HomeFragment";
    private FragmentHomeBinding binding;
    private HomeModel homeModel;
    private ResourceAdapter resourceAdapter;
    private Map<String, Object> lastAccessedData = null;

    public HomeFragment() {}

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        homeModel = new HomeModel();
        setupRecyclerView();
        setupButtonClickListeners();
        showLoading(true);

        try {
            homeModel.loadHomeData(this);
        } catch (SecurityException se) {
            Log.e(TAG, "Google API security error", se);
            showLoading(false);
            Toast.makeText(requireContext(), "Layanan tidak tersedia", Toast.LENGTH_LONG).show();
        }

        // Listener Tombol Notifikasi (Lonceng)
        if (binding.btnNotification != null) {
            binding.btnNotification.setOnClickListener(v -> {
                Intent intent = new Intent(requireContext(), NotificationActivity.class);
                startActivity(intent);
            });
        }
    }

    private void setupRecyclerView() {
        if (getContext() == null) return;
        binding.recyclerViewRecent.setLayoutManager(new LinearLayoutManager(requireContext()));
    }

    private void setupButtonClickListeners() {
        binding.btnBrowseAll.setOnClickListener(v -> {
            Intent intent = new Intent(requireActivity(), ResourceBrowseActivity.class);
            startActivity(intent);
        });

        binding.cardContinueLearning.setOnClickListener(v -> {
            if (lastAccessedData != null && lastAccessedData.containsKey("url")) {
                String urlToLoad = (String) lastAccessedData.get("url");
                String title = (String) lastAccessedData.get("title");

                if (urlToLoad == null || urlToLoad.isEmpty()) {
                    Toast.makeText(requireContext(), "URL resource tidak valid", Toast.LENGTH_SHORT).show();
                    return;
                }

                Log.d(TAG, "Membuka URL dari Lanjutkan Belajar: " + urlToLoad);

                Context context = requireContext();
                Intent intent = new Intent(context, WebViewActivity.class);

                // --- LOGIKA FINAL (HANYA MENGUBAH YOUTUBE) ---
                String lowerUrl = urlToLoad.toLowerCase();
                if (lowerUrl.contains("youtube.com/watch?v=")) {
                    try {
                        String videoId = urlToLoad.split("v=")[1].split("&")[0];
                        urlToLoad = "https://www.youtube.com/embed/" + videoId;
                    } catch (Exception e) {
                        Log.e(TAG, "Error parsing YouTube URL", e);
                    }
                }

                intent.putExtra(WebViewActivity.EXTRA_URL, urlToLoad);
                intent.putExtra(WebViewActivity.EXTRA_TITLE, title);
                context.startActivity(intent);

            } else {
                Toast.makeText(requireContext(), "Data resource terakhir tidak ditemukan", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showLoading(boolean isLoading) {
        if (binding == null) return;
        binding.progressBarHome.setVisibility(isLoading ? View.VISIBLE : View.GONE);
    }

    @Override
    public void onUserDataLoaded(String userName) {
        if (binding == null) return;
        binding.tvWelcomeMessage.setText("Selamat Datang, " + (userName != null ? userName : ""));
    }

    // --- PERBAIKAN UTAMA ADA DI SINI ---
    @Override
    public void onRecentResourcesLoaded(List<Resource> resources, List<String> documentIds) {
        if (binding == null) return;
        showLoading(false);

        if (resources == null) resources = new ArrayList<>();

        // KITA HANYA MENGIRIM 'resources' SAJA (1 PARAMETER)
        // Sesuai dengan ResourceAdapter baru yang kita buat sebelumnya
        resourceAdapter = new ResourceAdapter(resources);

        binding.recyclerViewRecent.setAdapter(resourceAdapter);
        binding.recyclerViewRecent.setVisibility(resources.isEmpty() ? View.GONE : View.VISIBLE);
    }

    @Override
    public void onLastAccessedLoaded(@Nullable Map<String, Object> lastAccessedData) {
        this.lastAccessedData = lastAccessedData;
        if (binding == null) return;
        if (lastAccessedData != null && lastAccessedData.containsKey("title")) {
            String title = (String) lastAccessedData.get("title");
            binding.tvContinueTitle.setText(title);
            binding.cardContinueLearning.setVisibility(View.VISIBLE);
        } else {
            binding.cardContinueLearning.setVisibility(View.GONE);
        }
    }

    @Override
    public void onDataLoadError(String error) {
        if (binding == null) return;
        showLoading(false);
        Toast.makeText(requireContext(), "Error: " + error, Toast.LENGTH_LONG).show();

        if (error != null && error.contains("Tidak ada user") && getActivity() != null) {
            try {
                homeModel.logout();
            } catch (Exception ignored) {}
            Intent intent = new Intent(getActivity(), MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            getActivity().finish();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}