package com.f52123078.aplikasibelajarmandiri.controller;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import com.f52123078.aplikasibelajarmandiri.R;
import com.f52123078.aplikasibelajarmandiri.controller.AdminDashboardActivity;
import com.f52123078.aplikasibelajarmandiri.controller.HomeActivity;
import com.f52123078.aplikasibelajarmandiri.databinding.ActivityRegisterBinding;
import com.f52123078.aplikasibelajarmandiri.model.AuthModel;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;

// Kita implementasikan LoginListener juga karena Google dianggap sebagai Login
public class RegisterActivity extends AppCompatActivity implements AuthModel.RegisterListener, AuthModel.LoginListener {

    private ActivityRegisterBinding binding;
    private AuthModel authModel;
    private GoogleSignInClient mGoogleSignInClient; // Tambahan untuk Google

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRegisterBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        authModel = new AuthModel();

        // 1. Setup Google Sign In Client
        setupGoogleSignIn();

        setupClickListeners();
    }

    private void setupGoogleSignIn() {
        // Pastikan default_web_client_id benar (dari google-services.json)
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
    }

    private void setupClickListeners() {
        binding.toolbar.setNavigationOnClickListener(v -> finish());

        // Link ke Login
        binding.layoutLoginLink.setOnClickListener(v -> finish());
        binding.tvGoToLogin.setOnClickListener(v -> finish());

        // Register Biasa
        binding.btnRegister.setOnClickListener(v -> handleRegister());

        // 2. Tombol Register via Google
        binding.btnGoogleRegister.setOnClickListener(v -> {
            showLoading(true);
            // Logout dulu agar selalu muncul popup pilih akun
            mGoogleSignInClient.signOut().addOnCompleteListener(this, task -> {
                Intent signInIntent = mGoogleSignInClient.getSignInIntent();
                googleSignInLauncher.launch(signInIntent);
            });
        });
    }

    // 3. Launcher Hasil Google
    private final ActivityResultLauncher<Intent> googleSignInLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK) {
                    Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(result.getData());
                    try {
                        GoogleSignInAccount account = task.getResult(ApiException.class);
                        // Panggil fungsi loginWithGoogle di Model
                        // Model akan otomatis mengecek apakah akun sudah ada atau belum
                        authModel.loginWithGoogle(account.getIdToken(), this);
                    } catch (ApiException e) {
                        showLoading(false);
                        Toast.makeText(this, "Google sign up failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    showLoading(false);
                }
            }
    );

    private void handleRegister() {
        String name = binding.etName.getText().toString().trim();
        String email = binding.etEmail.getText().toString().trim();
        String password = binding.etPassword.getText().toString().trim();
        String confirmPassword = binding.etConfirmPassword.getText().toString().trim();

        binding.tilName.setError(null);
        binding.tilEmail.setError(null);
        binding.tilPassword.setError(null);
        binding.tilConfirmPassword.setError(null);

        showLoading(true);
        authModel.registerUser(name, email, password, confirmPassword, this);
    }

    private void showLoading(boolean isLoading) {
        if (binding == null) return;
        binding.progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        binding.btnRegister.setEnabled(!isLoading);
        binding.btnGoogleRegister.setEnabled(!isLoading);
    }

    // --- Callback Register Manual ---
    @Override
    public void onRegisterSuccess() {
        showLoading(false);
        Toast.makeText(this, "Akun berhasil dibuat!", Toast.LENGTH_LONG).show();
        // Langsung ke Home atau Login (pilih salah satu)
        startActivity(new Intent(this, HomeActivity.class));
        finishAffinity();
    }

    @Override
    public void onRegisterFailure(String error) {
        showLoading(false);
        Toast.makeText(this, error, Toast.LENGTH_LONG).show();
        // Error handling khusus field (opsional, sudah ada di kode Anda sebelumnya)
    }

    // --- Callback Login Google (Implementasi LoginListener) ---
    @Override
    public void onLoginSuccess(String role) {
        showLoading(false);
        Toast.makeText(this, "Berhasil masuk dengan Google!", Toast.LENGTH_SHORT).show();

        // Arahkan sesuai role (biasanya user baru = user)
        if (role.equals("admin")) {
            startActivity(new Intent(this, AdminDashboardActivity.class));
        } else {
            startActivity(new Intent(this, HomeActivity.class));
        }
        finishAffinity(); // Tutup semua activity sebelumnya
    }

    @Override
    public void onLoginFailure(String error) {
        showLoading(false);
        Toast.makeText(this, "Gagal: " + error, Toast.LENGTH_LONG).show();
    }
}