package com.f52123078.aplikasibelajarmandiri.view;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.cloudinary.android.MediaManager;
import com.cloudinary.android.callback.ErrorInfo;
import com.cloudinary.android.callback.UploadCallback;
import com.f52123078.aplikasibelajarmandiri.databinding.FragmentAccountBinding;
import com.f52123078.aplikasibelajarmandiri.model.HomeModel;
import com.f52123078.aplikasibelajarmandiri.model.Resource;
import com.f52123078.aplikasibelajarmandiri.controller.MainActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AccountFragment extends Fragment implements HomeModel.HomeDataListener {

    private FragmentAccountBinding binding;
    private HomeModel homeModel;
    private FirebaseUser currentUser;
    private FirebaseFirestore db;
    private Uri selectedImageUri = null; // Menyimpan gambar baru jika dipilih

    public AccountFragment() {}

    // Launcher untuk memilih gambar dari galeri
    private final ActivityResultLauncher<Intent> galleryLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    selectedImageUri = result.getData().getData();
                    // Tampilkan preview langsung
                    if (binding != null) {
                        Glide.with(this).load(selectedImageUri).circleCrop().into(binding.ivProfilePicture);
                    }
                }
            }
    );

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentAccountBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        homeModel = new HomeModel();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        db = FirebaseFirestore.getInstance();

        if (currentUser == null) {
            goToLogin();
            return;
        }

        setupInitialData();

        // Listener Tombol
        binding.btnChangePhoto.setOnClickListener(v -> openGallery());
        binding.btnSaveProfile.setOnClickListener(v -> handleSave());
        binding.btnLogoutAccount.setOnClickListener(v -> {
            homeModel.logout();
            goToLogin();
        });
    }

    private void setupInitialData() {
        binding.etAccountEmail.setText(currentUser.getEmail());

        // Load Nama
        homeModel.loadUserName(currentUser.getUid(), this);

        // Load Foto Profil yang sudah ada (dari Firestore atau Auth)
        db.collection("users").document(currentUser.getUid()).get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists() && doc.getString("photoUrl") != null) {
                        String photoUrl = doc.getString("photoUrl");
                        if (!photoUrl.isEmpty() && binding != null) {
                            Glide.with(this).load(photoUrl).circleCrop().into(binding.ivProfilePicture);
                        }
                    }
                });
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        galleryLauncher.launch(intent);
    }

    private void handleSave() {
        String newName = binding.etAccountName.getText().toString().trim();
        if (newName.isEmpty()) {
            binding.tilAccountName.setError("Nama tidak boleh kosong");
            return;
        }
        binding.tilAccountName.setError(null);
        showLoading(true);

        // Skenario:
        // 1. Jika ada gambar baru -> Upload Cloudinary -> Dapat URL -> Simpan Nama & URL
        // 2. Jika TIDAK ada gambar baru -> Langsung Simpan Nama (URL lama tetap)

        if (selectedImageUri != null) {
            uploadToCloudinary(newName);
        } else {
            updateFirebaseData(newName, null); // Null artinya jangan ubah foto
        }
    }

    private void uploadToCloudinary(String newName) {
        MediaManager.get().upload(selectedImageUri)
                .callback(new UploadCallback() {
                    @Override
                    public void onStart(String requestId) {}

                    @Override
                    public void onProgress(String requestId, long bytes, long totalBytes) {}

                    @Override
                    public void onSuccess(String requestId, Map resultData) {
                        String imageUrl = (String) resultData.get("secure_url");
                        // Upload sukses, sekarang simpan URL ke database
                        if (getActivity() != null) {
                            getActivity().runOnUiThread(() -> updateFirebaseData(newName, imageUrl));
                        }
                    }

                    @Override
                    public void onError(String requestId, ErrorInfo error) {
                        if (getActivity() != null) {
                            getActivity().runOnUiThread(() -> {
                                showLoading(false);
                                Toast.makeText(getContext(), "Gagal upload foto: " + error.getDescription(), Toast.LENGTH_SHORT).show();
                            });
                        }
                    }

                    @Override
                    public void onReschedule(String requestId, ErrorInfo error) {}
                })
                .dispatch();
    }

    private void updateFirebaseData(String newName, @Nullable String newPhotoUrl) {
        // 1. Update Auth (User Profile)
        UserProfileChangeRequest.Builder builder = new UserProfileChangeRequest.Builder()
                .setDisplayName(newName);

        if (newPhotoUrl != null) {
            builder.setPhotoUri(Uri.parse(newPhotoUrl));
        }

        currentUser.updateProfile(builder.build())
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // 2. Update Firestore (Database)
                        updateFirestore(newName, newPhotoUrl);
                    } else {
                        showLoading(false);
                        Toast.makeText(getContext(), "Gagal update profil Auth", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void updateFirestore(String newName, @Nullable String newPhotoUrl) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("name", newName);
        if (newPhotoUrl != null) {
            updates.put("photoUrl", newPhotoUrl);
        }

        db.collection("users").document(currentUser.getUid())
                .update(updates)
                .addOnSuccessListener(aVoid -> {
                    showLoading(false);
                    Toast.makeText(getContext(), "Profil berhasil diperbarui!", Toast.LENGTH_SHORT).show();
                    selectedImageUri = null; // Reset pilihan
                })
                .addOnFailureListener(e -> {
                    showLoading(false);
                    Toast.makeText(getContext(), "Gagal update database: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    // --- IMPLEMENTASI INTERFACE HOMEMODEL ---
    @Override
    public void onUserDataLoaded(String userName) {
        // Hanya set nama jika user belum mengetik apa-apa (agar tidak menimpa editan user)
        if (binding != null && binding.etAccountName.getText().toString().isEmpty()) {
            binding.etAccountName.setText(userName != null ? userName : "");
        }
    }

    @Override
    public void onRecentResourcesLoaded(List<Resource> resources, List<String> ids) {}
    @Override
    public void onLastAccessedLoaded(@Nullable Map<String, Object> lastAccessedData) {}
    @Override
    public void onDataLoadError(String error) {
        // Error loading initial data, abaikan agar tidak mengganggu user
    }

    private void showLoading(boolean isLoading) {
        if (binding == null) return;
        binding.progressBarAccount.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        binding.btnSaveProfile.setEnabled(!isLoading);
        binding.btnChangePhoto.setEnabled(!isLoading);
        binding.etAccountName.setEnabled(!isLoading);
    }

    private void goToLogin() {
        if (getActivity() == null) return;
        Intent intent = new Intent(getActivity(), MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        getActivity().finish();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}