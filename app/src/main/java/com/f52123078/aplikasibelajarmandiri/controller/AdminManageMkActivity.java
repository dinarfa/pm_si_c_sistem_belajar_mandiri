package com.f52123078.aplikasibelajarmandiri.controller;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.f52123078.aplikasibelajarmandiri.databinding.ActivityAdminManageMkBinding;
import com.f52123078.aplikasibelajarmandiri.model.AdminManageMkModel;
import com.f52123078.aplikasibelajarmandiri.model.MataKuliah;
import com.f52123078.aplikasibelajarmandiri.model.Prodi;
import java.util.List;

public class AdminManageMkActivity extends AppCompatActivity implements AdminManageMkModel.DataListener, AdminMkAdapter.OnActionClickListener {

    private ActivityAdminManageMkBinding binding;
    private AdminManageMkModel model;
    private List<Prodi> prodiList;
    private boolean isEditMode = false;
    private String editId = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAdminManageMkBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        model = new AdminManageMkModel();
        binding.rvMk.setLayoutManager(new LinearLayoutManager(this));
        binding.toolbar.setNavigationOnClickListener(v -> finish());

        binding.btnSave.setOnClickListener(v -> saveMk());
        binding.btnCancel.setOnClickListener(v -> resetForm());

        binding.progressBar.setVisibility(View.VISIBLE);
        model.loadProdi(this);
        model.loadMkRealtime(this);
    }

    private void saveMk() {
        String name = binding.etMk.getText().toString().trim();
        String prodiName = binding.actProdi.getText().toString();
        Prodi selectedProdi = getSelectedProdi(prodiName);

        if (name.isEmpty() || selectedProdi == null) {
            Toast.makeText(this, "Isi nama dan pilih prodi!", Toast.LENGTH_SHORT).show();
            return;
        }
        binding.btnSave.setEnabled(false);

        MataKuliah mk = new MataKuliah();
        mk.setName(name);
        mk.setProdiId(selectedProdi.getDocumentId());
        mk.setProdiName(selectedProdi.getName());

        if (isEditMode) {
            mk.setId(editId); // Set ID lama agar tidak hilang di object (opsional)
            model.updateMk(editId, mk, this);
        } else {
            model.addMk(mk, this);
        }
    }

    private Prodi getSelectedProdi(String name) {
        if (prodiList == null) return null;
        for (Prodi p : prodiList) if (p.getName().equals(name)) return p;
        return null;
    }

    private void resetForm() {
        binding.etMk.setText("");
        binding.actProdi.setText("", false);
        binding.tvFormTitle.setText("Tambah Mata Kuliah");
        binding.btnSave.setText("Simpan");
        binding.btnCancel.setVisibility(View.GONE);
        isEditMode = false;
        editId = null;
    }

    @Override
    public void onProdiLoaded(List<Prodi> list) {
        this.prodiList = list;
        binding.actProdi.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, list));
    }

    @Override
    public void onMkLoaded(List<MataKuliah> list) {
        binding.progressBar.setVisibility(View.GONE);
        binding.rvMk.setAdapter(new AdminMkAdapter(list, this));
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
    public void onEdit(MataKuliah mk) {
        isEditMode = true;
        editId = mk.getDocumentId();
        binding.etMk.setText(mk.getName());
        binding.actProdi.setText(mk.getProdiName(), false);
        binding.tvFormTitle.setText("Edit Mata Kuliah");
        binding.btnSave.setText("Update");
        binding.btnCancel.setVisibility(View.VISIBLE);
    }

    @Override
    public void onDelete(MataKuliah mk) {
        new AlertDialog.Builder(this).setTitle("Hapus MK").setMessage("Hapus " + mk.getName() + "?")
                .setPositiveButton("Hapus", (d, w) -> model.deleteMk(mk.getDocumentId(), this))
                .setNegativeButton("Batal", null).show();
    }

    @Override
    protected void onDestroy() { super.onDestroy(); model.detachListener(); }
}