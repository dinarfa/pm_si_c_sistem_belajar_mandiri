package com.f52123078.aplikasibelajarmandiri.controller;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.f52123078.aplikasibelajarmandiri.databinding.ActivityResourceBrowseBinding;
import com.f52123078.aplikasibelajarmandiri.model.Prodi;
import com.f52123078.aplikasibelajarmandiri.model.MataKuliah;
import com.f52123078.aplikasibelajarmandiri.model.Resource;
import com.f52123078.aplikasibelajarmandiri.model.ResourceBrowseModel;
import com.google.android.material.chip.Chip;

import java.util.ArrayList;
import java.util.List;

public class ResourceBrowseActivity extends AppCompatActivity implements ResourceBrowseModel.BrowseDataListener {

    private ActivityResourceBrowseBinding binding;
    private ResourceBrowseModel model;
    private ResourceAdapter adapter;

    // Menyimpan data yang sedang tampil (sesuai Prodi) untuk keperluan pencarian lokal
    private List<Resource> currentList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityResourceBrowseBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        model = new ResourceBrowseModel();

        setupToolbar();
        setupRecyclerView();
        setupSearchAndFilter();

        // Load Data Awal (Semua Jurusan)
        showLoading(true);
        model.loadProdi(this);
        model.loadFilteredResources(null, null, this);
    }

    private void setupToolbar() {
        binding.toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void setupRecyclerView() {
        binding.rvResourceResults.setLayoutManager(new LinearLayoutManager(this));
        // Inisialisasi adapter kosong dulu
        adapter = new ResourceAdapter(new ArrayList<>());
        binding.rvResourceResults.setAdapter(adapter);
    }

    private void setupSearchAndFilter() {
        // 1. LISTENER PENCARIAN (Real-time Search)
        // Mencari Judul ATAU Mata Kuliah di dalam list yang sedang aktif
        binding.etSearchBrowse.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterListLocal(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // 2. LISTENER FILTER PRODI (Chip)
        binding.chipGroupFilter.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.isEmpty()) {
                // Jika user uncheck semua chip, kembali ke "Semua"
                loadResourcesByProdi(null);
            } else {
                int id = checkedIds.get(0);
                Chip chip = group.findViewById(id);
                if (chip != null) {
                    // Ambil ID Prodi yang tersimpan di Tag chip
                    String prodiId = (String) chip.getTag();
                    loadResourcesByProdi(prodiId);
                }
            }
        });
    }

    // --- LOGIKA UTAMA ---

    /**
     * Mengambil data baru dari Firestore berdasarkan Prodi.
     * Saat Prodi berubah, Search Bar dikosongkan agar tidak bingung.
     */
    private void loadResourcesByProdi(String prodiId) {
        showLoading(true);
        binding.etSearchBrowse.setText(""); // Reset pencarian
        model.loadFilteredResources(prodiId, null, this);
    }

    /**
     * Melakukan filter lokal pada list yang sudah didownload (currentList).
     * Mencocokkan query dengan Judul Resource ATAU Nama Mata Kuliah.
     */
    private void filterListLocal(String query) {
        String lowerQuery = query.toLowerCase().trim();
        List<Resource> filteredList = new ArrayList<>();

        for (Resource res : currentList) {
            // Cek Judul
            boolean matchTitle = res.getTitle() != null && res.getTitle().toLowerCase().contains(lowerQuery);

            // Cek Nama Mata Kuliah (Pastikan di Resource.java method getCourse() sudah benar)
            boolean matchCourse = res.getCourse() != null && res.getCourse().toLowerCase().contains(lowerQuery);

            if (matchTitle || matchCourse) {
                filteredList.add(res);
            }
        }

        updateAdapterUI(filteredList);
    }

    // Helper untuk update UI List
    private void updateAdapterUI(List<Resource> list) {
        adapter.updateList(list);

        if (list.isEmpty()) {
            binding.tvEmptyBrowse.setVisibility(View.VISIBLE);
            binding.rvResourceResults.setVisibility(View.GONE);
        } else {
            binding.tvEmptyBrowse.setVisibility(View.GONE);
            binding.rvResourceResults.setVisibility(View.VISIBLE);
        }
    }

    private void showLoading(boolean isLoading) {
        binding.progressBarBrowse.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        // Sembunyikan list saat loading agar tidak flicker
        if (isLoading) {
            binding.rvResourceResults.setVisibility(View.GONE);
            binding.tvEmptyBrowse.setVisibility(View.GONE);
        }
    }

    // --- CALLBACK DARI MODEL ---

    @Override
    public void onProdiLoaded(List<Prodi> prodiList) {
        // Menambahkan Chip Prodi secara dinamis
        for (Prodi prodi : prodiList) {
            Chip chip = new Chip(this);
            chip.setText(prodi.getName());
            chip.setTag(prodi.getDocumentId()); // Simpan ID Prodi di sini
            chip.setCheckable(true);

            chip.setChipBackgroundColorResource(android.R.color.transparent);
            chip.setChipStrokeColorResource(android.R.color.darker_gray);
            chip.setChipStrokeWidth(1f); // Ketebalan garis pinggir
            chip.setTextColor(getResources().getColor(android.R.color.black));

            binding.chipGroupFilter.addView(chip);
        }
    }

    @Override
    public void onMataKuliahLoaded(List<MataKuliah> mkList) {
        // Tidak digunakan lagi karena filter MK digabung ke Search Bar
    }

    @Override
    public void onResourcesFiltered(List<Resource> resourceList, List<String> documentIds) {
        showLoading(false);
        if (resourceList == null) resourceList = new ArrayList<>();

        // 1. Simpan data asli dari database ke variabel global
        this.currentList = new ArrayList<>(resourceList);

        // 2. Tampilkan semua data (karena search bar masih kosong saat baru load)
        updateAdapterUI(resourceList);
    }

    @Override
    public void onError(String error) {
        showLoading(false);
        Toast.makeText(this, error, Toast.LENGTH_LONG).show();
    }
}