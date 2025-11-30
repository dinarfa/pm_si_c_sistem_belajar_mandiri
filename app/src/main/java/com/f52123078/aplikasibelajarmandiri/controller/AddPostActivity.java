package com.f52123078.aplikasibelajarmandiri.controller;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide;
import com.cloudinary.android.MediaManager;
import com.cloudinary.android.callback.ErrorInfo;
import com.cloudinary.android.callback.UploadCallback;
import com.f52123078.aplikasibelajarmandiri.databinding.ActivityAddPostBinding;
import com.f52123078.aplikasibelajarmandiri.model.ForumPost;
import com.f52123078.aplikasibelajarmandiri.model.MataKuliah;
import com.f52123078.aplikasibelajarmandiri.model.Prodi;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.List;
import java.util.Map;

public class AddPostActivity extends AppCompatActivity {

    private ActivityAddPostBinding binding;
    private Uri selectedImageUri = null;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private String selectedProdiName = "";
    private String selectedMkName = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAddPostBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        setupDropdowns();

        binding.btnClose.setOnClickListener(v -> finish());
        binding.btnSelectImage.setOnClickListener(v -> openGallery());
        binding.btnRemoveImage.setOnClickListener(v -> {
            selectedImageUri = null;
            binding.ivPreview.setVisibility(View.GONE);
            binding.btnRemoveImage.setVisibility(View.GONE);
            checkInputValidity();
        });
        binding.btnPost.setOnClickListener(v -> handlePost());

        binding.etPostText.addTextChangedListener(new android.text.TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) { checkInputValidity(); }
            @Override public void afterTextChanged(android.text.Editable s) {}
        });
    }

    private void setupDropdowns() {
        db.collection("prodi").orderBy("name").get().addOnSuccessListener(snapshot -> {
            List<Prodi> prodiList = snapshot.toObjects(Prodi.class);
            ArrayAdapter<Prodi> adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, prodiList);
            binding.actProdiPost.setAdapter(adapter);
        });

        binding.actProdiPost.setOnItemClickListener((parent, view, position, id) -> {
            Prodi selected = (Prodi) parent.getItemAtPosition(position);
            selectedProdiName = selected.getName();

            binding.actMkPost.setText("", false);
            binding.actMkPost.setEnabled(true);
            db.collection("mata_kuliah")
                    .whereEqualTo("prodiId", selected.getDocumentId())
                    .orderBy("name")
                    .get()
                    .addOnSuccessListener(snapshot -> {
                        List<MataKuliah> mkList = snapshot.toObjects(MataKuliah.class);
                        ArrayAdapter<MataKuliah> mkAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, mkList);
                        binding.actMkPost.setAdapter(mkAdapter);
                    });
        });

        binding.actMkPost.setOnItemClickListener((parent, view, position, id) -> {
            MataKuliah selected = (MataKuliah) parent.getItemAtPosition(position);
            selectedMkName = selected.getName();
            checkInputValidity();
        });
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        galleryLauncher.launch(intent);
    }

    private final ActivityResultLauncher<Intent> galleryLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    selectedImageUri = result.getData().getData();
                    binding.ivPreview.setVisibility(View.VISIBLE);
                    binding.btnRemoveImage.setVisibility(View.VISIBLE);
                    Glide.with(this).load(selectedImageUri).into(binding.ivPreview);
                    checkInputValidity();
                }
            }
    );

    private void handlePost() {
        String text = binding.etPostText.getText().toString().trim();
        if (text.isEmpty()) return;

        if (selectedMkName.isEmpty()) {
            Toast.makeText(this, "Pilih Mata Kuliah dulu!", Toast.LENGTH_SHORT).show();
            return;
        }

        setLoading(true);
        if (selectedImageUri != null) {
            uploadToCloudinary(text);
        } else {
            saveToFirestore(text, null);
        }
    }

    private void uploadToCloudinary(String text) {
        MediaManager.get().upload(selectedImageUri).callback(new UploadCallback() {
            @Override public void onStart(String requestId) {}
            @Override public void onProgress(String requestId, long bytes, long totalBytes) {}
            @Override public void onSuccess(String requestId, Map resultData) {
                runOnUiThread(() -> saveToFirestore(text, (String) resultData.get("secure_url")));
            }
            @Override public void onError(String requestId, ErrorInfo error) {
                runOnUiThread(() -> {
                    setLoading(false);
                    Toast.makeText(AddPostActivity.this, "Upload Gagal", Toast.LENGTH_SHORT).show();
                });
            }
            @Override public void onReschedule(String requestId, ErrorInfo error) {}
        }).dispatch();
    }

    private void saveToFirestore(String text, @Nullable String imageUrl) {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) return;

        db.collection("users").document(user.getUid()).get().addOnSuccessListener(doc -> {
            String name = doc.getString("name");
            String photo = doc.getString("photoUrl");

            ForumPost post = new ForumPost(
                    user.getUid(), name != null ? name : "User", photo,
                    text, imageUrl,
                    selectedProdiName, selectedMkName
            );

            db.collection("forum_posts").add(post)
                    .addOnSuccessListener(d -> {
                        setLoading(false);
                        Toast.makeText(this, "Terkirim!", Toast.LENGTH_SHORT).show();
                        finish();
                    })
                    .addOnFailureListener(e -> setLoading(false));
        });
    }

    private void checkInputValidity() {
        String text = binding.etPostText.getText().toString().trim();
        binding.btnPost.setEnabled(!text.isEmpty());
    }

    private void setLoading(boolean isLoading) {
        binding.loadingOverlay.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        binding.btnPost.setEnabled(!isLoading);
    }
}