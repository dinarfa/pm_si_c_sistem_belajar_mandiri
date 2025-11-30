package com.f52123078.aplikasibelajarmandiri.controller;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.f52123078.aplikasibelajarmandiri.databinding.ActivityAdminDashboardBinding;
import com.google.firebase.auth.FirebaseAuth;

public class AdminDashboardActivity extends AppCompatActivity {

    private ActivityAdminDashboardBinding binding;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAdminDashboardBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mAuth = FirebaseAuth.getInstance();
        setupClickListeners();
    }

    private void setupClickListeners() {
        // 1. Tombol Logout (Custom Header)
        binding.btnLogoutHeader.setOnClickListener(v -> {
            mAuth.signOut();
            Intent intent = new Intent(AdminDashboardActivity.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });

        // 2. Card Kelola Resources
        binding.cardManageResources.setOnClickListener(v -> {
            Intent intent = new Intent(AdminDashboardActivity.this, AdminManageResourceActivity.class);
            startActivity(intent);
        });

        // 3. Card Kelola Prodi
        binding.cardManageProdi.setOnClickListener(v -> {
            startActivity(new Intent(AdminDashboardActivity.this, AdminManageProdiActivity.class));
        });

        // 4. Card Kelola MK
        binding.cardManageMk.setOnClickListener(v -> {
            startActivity(new Intent(AdminDashboardActivity.this, AdminManageMkActivity.class));
        });

        // 5. Card Kelola Users
        binding.cardManageUsers.setOnClickListener(v -> {
            startActivity(new Intent(AdminDashboardActivity.this, AdminManageUsersActivity.class));
        });
    }
}