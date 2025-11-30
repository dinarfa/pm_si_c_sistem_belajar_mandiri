package com.f52123078.aplikasibelajarmandiri.controller;


import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.f52123078.aplikasibelajarmandiri.databinding.ActivityAdminManageResourceBinding;
import com.f52123078.aplikasibelajarmandiri.model.AdminManageResourceModel;
import com.f52123078.aplikasibelajarmandiri.model.MataKuliah;
import com.f52123078.aplikasibelajarmandiri.model.Prodi;
import com.f52123078.aplikasibelajarmandiri.model.Resource;

import java.util.List;

public class AdminManageResourceActivity extends AppCompatActivity
        implements AdminManageResourceModel.DataListener, AdminResourceAdapter.OnResourceActionClicked {


    // View
    private ActivityAdminManageResourceBinding binding;

    // Model
    private AdminManageResourceModel model;

    // Adapter
    private AdminResourceAdapter adapter;

    // State untuk Dropdown
    private List<Prodi> prodiList;
    private List<MataKuliah> mkList;

    // State untuk Mode Edit
    private boolean isEditMode = false;
    private String currentEditDocId = null;
    // --- PERBAIKAN 1: Simpan objek resource saat diedit ---
    private Resource resourceToEdit = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAdminManageResourceBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Inisialisasi Model
        model = new AdminManageResourceModel();

        // Setup awal
        setupToolbar();
        setupRecyclerView();
        setupFormListeners();

        // Mulai memuat data
        showListLoading(true);
        model.loadProdi(this); // 1. Muat Prodi dulu
        model.loadResourcesRealtime(this); // 3. Muat list resource
    }

    // --- Setup Tampilan ---

    private void setupToolbar() {
        // Tombol back
        binding.toolbarAdminManage.setNavigationOnClickListener(v -> finish());
    }

    private void setupRecyclerView() {
        binding.recyclerViewAdminResources.setLayoutManager(new LinearLayoutManager(this));
    }

    private void setupFormListeners() {
        // Tipe Resource Statis
        String[] types = new String[] {"youtube", "pdf", "website", "other"};
        ArrayAdapter<String> typeAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, types);
        binding.actType.setAdapter(typeAdapter);

        // Listener Dropdown Prodi
        binding.actProdi.setOnItemClickListener((parent, view, position, id) -> {
            Prodi selectedProdi = (Prodi) parent.getItemAtPosition(position);
            // 2. Muat MK berdasarkan prodi yang dipilih
            binding.actMk.setText("", false); // Kosongkan MK
            binding.actMk.setEnabled(false);
            if (selectedProdi != null) {
                // --- PERBAIKAN 2: Gunakan getDocumentId() ---
                model.loadMataKuliah(selectedProdi.getDocumentId(), this);
            }
        });

        // Listener Tombol Simpan
        binding.btnSave.setOnClickListener(v -> handleSave());

        // Listener Tombol Batal
        binding.btnCancel.setOnClickListener(v -> resetForm());
    }

    // --- Logika Form (Controller) ---

    private void handleSave() {
        // 1. Ambil data dari form
        String title = binding.etTitle.getText().toString().trim();
        String url = binding.etUrl.getText().toString().trim();
        String desc = binding.etDesc.getText().toString().trim();
        String type = binding.actType.getText().toString().trim();

        // Ambil objek dari AutoCompleteTextView
        Prodi selectedProdi = getSelectedProdi(binding.actProdi.getText().toString());
        MataKuliah selectedMK = getSelectedMK(binding.actMk.getText().toString());

        // 2. Validasi
        if (title.isEmpty() || url.isEmpty() || type.isEmpty() || selectedProdi == null || selectedMK == null) {
            Toast.makeText(this, "Harap isi semua field (Judul, URL, Tipe, Prodi, MK)", Toast.LENGTH_LONG).show();
            return;
        }

        // 3. Buat Objek Resource
        Resource resource = new Resource();
        resource.setTitle(title);
        resource.setUrl(url);
        resource.setDesc(desc);
        resource.setType(type);
        // Simpan data relasi (denormalisasi)
        resource.setCategory(selectedProdi.getName());
        resource.setCourse(selectedMK.getName());
        // --- PERBAIKAN 3: Gunakan getDocumentId() ---
        resource.setProdiId(selectedProdi.getDocumentId());
        resource.setMkId(selectedMK.getDocumentId());
        // createdAt akan diisi otomatis jika Anda set @ServerTimestamp di Model

        // 4. Panggil Model
        binding.btnSave.setEnabled(false);
        binding.btnSave.setText("Menyimpan...");

        if (isEditMode && currentEditDocId != null) {
            // Mode Update
            model.updateResource(currentEditDocId, resource, this);
        } else {
            // Mode Create
            model.addResource(resource, this);
        }
    }

    private void resetForm() {
        binding.etTitle.setText("");
        binding.etUrl.setText("");
        binding.etDesc.setText("");
        binding.actProdi.setText("", false);
        binding.actMk.setText("", false);
        binding.actType.setText("", false);

        binding.actMk.setEnabled(false);

        binding.tvFormTitle.setText("Tambah Resource Baru");
        binding.btnSave.setText("Simpan");
        binding.btnCancel.setVisibility(View.GONE);

        isEditMode = false;
        currentEditDocId = null;
        // --- PERBAIKAN 4: Bersihkan resource yang diedit ---
        resourceToEdit = null;

        binding.etTitle.requestFocus();
    }

    // --- Callback dari Adapter (RecyclerView) ---

    @Override
    public void onEditClicked(Resource resource, String documentId) {
        // Isi form dengan data yang ada
        binding.tvFormTitle.setText("Edit Resource");
        binding.etTitle.setText(resource.getTitle());
        binding.etUrl.setText(resource.getUrl());
        binding.etDesc.setText(resource.getDesc());
        binding.actType.setText(resource.getType(), false);

        // Set dropdown Prodi
        binding.actProdi.setText(resource.getCategory(), false);

        // Load MK untuk prodi ini, lalu set nilainya
        model.loadMataKuliah(resource.getProdiId(), this);

        isEditMode = true;
        currentEditDocId = documentId;
        // --- PERBAIKAN 5: Simpan objek resource di sini ---
        resourceToEdit = resource;

        binding.btnCancel.setVisibility(View.VISIBLE);
        binding.btnSave.setText("Update");

        // Scroll ke atas
        binding.getRoot().getParent().requestChildFocus(binding.getRoot(), binding.getRoot());
        binding.etTitle.requestFocus();
    }

    @Override
    public void onDeleteClicked(String documentId, String resourceTitle) {
        // Tampilkan dialog konfirmasi (Prinsip HCI: Mencegah error)
        new AlertDialog.Builder(this)
                .setTitle("Hapus Resource")
                .setMessage("Anda yakin ingin menghapus \"" + resourceTitle + "\"?")
                .setPositiveButton("Hapus", (dialog, which) -> {
                    model.deleteResource(documentId, this);
                })
                .setNegativeButton("Batal", null)
                .show();
    }

    // --- Callback dari Model (Firestore) ---

    @Override
    public void onProdiLoaded(List<Prodi> prodiList) {
        this.prodiList = prodiList;
        // Tampilkan di dropdown
        ArrayAdapter<Prodi> prodiAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, prodiList);
        binding.actProdi.setAdapter(prodiAdapter);
    }

    @Override
    public void onMataKuliahLoaded(List<MataKuliah> mkList) {
        this.mkList = mkList;
        // Tampilkan di dropdown
        ArrayAdapter<MataKuliah> mkAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, mkList);
        binding.actMk.setAdapter(mkAdapter);
        binding.actMk.setEnabled(true);

        // --- PERBAIKAN 6: Gunakan objek resourceToEdit ---
        // Jika mode edit, set value MK sekarang
        if(isEditMode && resourceToEdit != null) {
            // Resource resourceToEdit = getResourceById(currentEditDocId); // <-- INI TIDAK PERLU LAGI
            if(resourceToEdit != null) {
                binding.actMk.setText(resourceToEdit.getCourse(), false);
            }
        }
    }

    @Override
    public void onResourcesLoaded(List<Resource> resourceList, List<String> documentIds) {
        showListLoading(false);
        adapter = new AdminResourceAdapter(resourceList, documentIds, this);
        binding.recyclerViewAdminResources.setAdapter(adapter);

        // Tampilkan pesan jika list kosong
        if(resourceList.isEmpty()) {
            binding.tvEmptyList.setVisibility(View.VISIBLE);
        } else {
            binding.tvEmptyList.setVisibility(View.GONE);
        }
    }

    @Override
    public void onWriteSuccess(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        binding.btnSave.setEnabled(true);
        resetForm();
    }

    @Override
    public void onError(String error) {
        showListLoading(false);
        binding.btnSave.setEnabled(true);
        binding.btnSave.setText(isEditMode ? "Update" : "Simpan");
        Toast.makeText(this, error, Toast.LENGTH_LONG).show();
    }

    // --- Helper ---

    private void showListLoading(boolean isLoading) {
        binding.progressBarList.setVisibility(isLoading ? View.VISIBLE :View.GONE);
        binding.recyclerViewAdminResources.setVisibility(isLoading ? View.GONE : View.VISIBLE);
        binding.tvEmptyList.setVisibility(View.GONE);
    }

    // Helper untuk mencari objek dari string
    private Prodi getSelectedProdi(String prodiName) {
        if (prodiList == null) return null;
        for (Prodi prodi : prodiList) {
            if (prodi.getName().equals(prodiName)) {
                return prodi;
            }
        }
        return null;
    }

    private MataKuliah getSelectedMK(String mkName) {
        if (mkList == null) return null;
        for (MataKuliah mk : mkList) {
            if (mk.getName().equals(mkName)) {
                return mk;
            }
        }
        return null;
    }

    // --- PERBAIKAN 7: Hapus method helper yang error ---
    // private Resource getResourceById(String docId) { ... }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Hentikan listener realtime
        model.detachResourceListener();
    }
}

