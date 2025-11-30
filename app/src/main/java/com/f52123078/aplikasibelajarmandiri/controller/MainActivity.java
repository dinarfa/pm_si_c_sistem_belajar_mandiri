package com.f52123078.aplikasibelajarmandiri.controller;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import com.f52123078.aplikasibelajarmandiri.R;
import com.f52123078.aplikasibelajarmandiri.databinding.ActivityMainBinding;
import com.f52123078.aplikasibelajarmandiri.model.AuthModel;
// PERBAIKAN IMPORT DI SINI:
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.Task;

public class MainActivity extends AppCompatActivity implements AuthModel.LoginListener {

    private ActivityMainBinding binding;
    private AuthModel authModel;
    private GoogleSignInClient mGoogleSignInClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        authModel = new AuthModel();
        setupGoogleSignIn();

        binding.btnLogin.setOnClickListener(v -> handleLogin());
        binding.tvGoToRegister.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, RegisterActivity.class)));

        // Listener Tombol Google (DIPERBAIKI)
        binding.btnGoogleLogin.setOnClickListener(v -> {
            showLoading(true);

            // --- INI KUNCINYA: Logout dulu dari Google Client ---
            mGoogleSignInClient.signOut().addOnCompleteListener(this, task -> {
                // Setelah berhasil logout, baru buka jendela pilih akun
                Intent signInIntent = mGoogleSignInClient.getSignInIntent();
                googleSignInLauncher.launch(signInIntent);
            });
        });
    }

    private void setupGoogleSignIn() {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
    }

    private final ActivityResultLauncher<Intent> googleSignInLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK) {
                    Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(result.getData());
                    try {
                        GoogleSignInAccount account = task.getResult(ApiException.class);
                        authModel.loginWithGoogle(account.getIdToken(), this);
                    } catch (ApiException e) {
                        showLoading(false);
                        Toast.makeText(this, "Google sign in failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    showLoading(false);
                }
            }
    );

    private void handleLogin() {
        String email = binding.etEmail.getText().toString().trim();
        String password = binding.etPassword.getText().toString().trim();
        showLoading(true);
        authModel.loginUser(email, password, this);
    }

    private void showLoading(boolean isLoading) {
        if (binding == null) return;
        binding.progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        binding.btnLogin.setEnabled(!isLoading);
        binding.btnGoogleLogin.setEnabled(!isLoading);
    }

    @Override
    public void onLoginSuccess(String role) {
        showLoading(false);
        Toast.makeText(this, "Login Berhasil!", Toast.LENGTH_SHORT).show();
        if (role.equals("admin")) {
            startActivity(new Intent(MainActivity.this, AdminDashboardActivity.class));
        } else {
            startActivity(new Intent(MainActivity.this, HomeActivity.class));
        }
        finish();
    }

    @Override
    public void onLoginFailure(String error) {
        showLoading(false);
        Toast.makeText(this, error, Toast.LENGTH_LONG).show();
    }
}