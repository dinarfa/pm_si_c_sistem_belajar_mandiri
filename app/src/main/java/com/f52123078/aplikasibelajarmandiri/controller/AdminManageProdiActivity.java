package com.f52123078.aplikasibelajarmandiri.controller;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.f52123078.aplikasibelajarmandiri.databinding.ActivityAdminManageProdiBinding;
import com.f52123078.aplikasibelajarmandiri.model.AdminManageProdiModel;
import com.f52123078.aplikasibelajarmandiri.model.Prodi;
import java.util.List;

public class AdminManageProdiActivity extends AppCompatActivity implements AdminManageProdiModel.DataListener, AdminProdiAdapter.OnActionClickListener {

    private ActivityAdminManageProdiBinding binding;
    private AdminManageProdiModel model;
    private boolean isEditMode = false;
    private String editId = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAdminManageProdiBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        model = new AdminManageProdiModel();
        binding.rvProdi.setLayoutManager(new LinearLayoutManager(this));
        binding.toolbar.setNavigationOnClickListener(v -> finish());

        binding.btnSave.setOnClickListener(v -> saveProdi());
        binding.btnCancel.setOnClickListener(v -> resetForm());

        binding.progressBar.setVisibility(View.VISIBLE);
        model.loadDataRealtime(this);
    }

    private void saveProdi() {
        String name = binding.etProdi.getText().toString().trim();
        if (name.isEmpty()) {
            binding.tilProdi.setError("Nama wajib diisi");
            return;
        }
        binding.tilProdi.setError(null);
        binding.btnSave.setEnabled(false);

        if (isEditMode) {
            model.updateProdi(editId, name, this);
        } else {
            Prodi p = new Prodi();
            // ID akan digenerate Firestore jika kosong di konstruktor atau saat add()
            // Kita pakai map di model, jadi di sini set nama saja
            // Tapi karena kita pakai POJO, model perlu disesuaikan sedikit atau pakai Map
            // Agar simpel, kita modifikasi model addProdi di atas agar terima objek atau biarkan
            // Untuk POJO Prodi yang Anda punya, field 'id' adalah @DocumentId, jadi aman.
            // Namun kita perlu mengirim data nama.
            // Karena Prodi class Anda simple, kita buat instance baru.
            // Note: Model addProdi pakai .add(object), firestore auto ID.
            model.addProdi(new Prodi(null, name), this);
        }
    }

    private void resetForm() {
        binding.etProdi.setText("");
        binding.tvFormTitle.setText("Tambah Prodi Baru");
        binding.btnSave.setText("Simpan");
        binding.btnCancel.setVisibility(View.GONE);
        isEditMode = false;
        editId = null;
    }

    @Override
    public void onDataLoaded(List<Prodi> list) {
        binding.progressBar.setVisibility(View.GONE);
        binding.rvProdi.setAdapter(new AdminProdiAdapter(list, this));
    }

    @Override
    public void onWriteSuccess(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        binding.btnSave.setEnabled(true);
        resetForm();
    }

    @Override
    public void onError(String error) {
        binding.btnSave.setEnabled(true);
        Toast.makeText(this, error, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onEdit(Prodi prodi) {
        isEditMode = true;
        editId = prodi.getDocumentId();
        binding.etProdi.setText(prodi.getName());
        binding.tvFormTitle.setText("Edit Prodi");
        binding.btnSave.setText("Update");
        binding.btnCancel.setVisibility(View.VISIBLE);
    }

    @Override
    public void onDelete(Prodi prodi) {
        new AlertDialog.Builder(this)
                .setTitle("Hapus Prodi")
                .setMessage("Hapus " + prodi.getName() + "?")
                .setPositiveButton("Hapus", (d, w) -> model.deleteProdi(prodi.getDocumentId(), this))
                .setNegativeButton("Batal", null).show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        model.detachListener();
    }
}