package com.f52123078.aplikasibelajarmandiri.controller;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.f52123078.aplikasibelajarmandiri.databinding.ActivityAdminManageUsersBinding;
import com.f52123078.aplikasibelajarmandiri.model.AdminManageUserModel;
import com.f52123078.aplikasibelajarmandiri.model.User;
import java.util.List;

public class AdminManageUsersActivity extends AppCompatActivity implements AdminManageUserModel.DataListener {

    private ActivityAdminManageUsersBinding binding;
    private AdminManageUserModel model;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAdminManageUsersBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        model = new AdminManageUserModel();

        setupToolbar();
        setupRecyclerView();

        // Load Data
        binding.progressBar.setVisibility(View.VISIBLE);
        model.loadUsersRealtime(this);
    }

    private void setupToolbar() {
        binding.toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void setupRecyclerView() {
        binding.rvUsers.setLayoutManager(new LinearLayoutManager(this));
    }

    @Override
    public void onUsersLoaded(List<User> userList) {
        binding.progressBar.setVisibility(View.GONE);

        // Update Counter
        binding.tvTotalUsers.setText("Total Pengguna: " + userList.size());

        if (userList.isEmpty()) {
            binding.tvEmpty.setVisibility(View.VISIBLE);
            binding.rvUsers.setVisibility(View.GONE);
        } else {
            binding.tvEmpty.setVisibility(View.GONE);
            binding.rvUsers.setVisibility(View.VISIBLE);
            binding.rvUsers.setAdapter(new com.f52123078.aplikasibelajarmandiri.viewModel.AdminUserAdapter(userList));
        }
    }

    @Override
    public void onError(String error) {
        binding.progressBar.setVisibility(View.GONE);
        Toast.makeText(this, "Error: " + error, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        model.detachListener();
    }
}