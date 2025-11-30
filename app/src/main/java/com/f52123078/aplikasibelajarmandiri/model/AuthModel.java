package com.f52123078.aplikasibelajarmandiri.model;

import android.util.Log;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.HashMap;
import java.util.Map;

public class AuthModel {

    private static final String TAG = "AuthModel";

    public interface LoginListener {
        void onLoginSuccess(String role);
        void onLoginFailure(String error);
    }

    public interface RegisterListener {
        void onRegisterSuccess();
        void onRegisterFailure(String error);
    }

    private final FirebaseAuth mAuth;
    private final FirebaseFirestore db;

    public AuthModel() {
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
    }

    // --- LOGIN EMAIL BIASA ---
    public void loginUser(String email, String password, final LoginListener listener) {
        if (email.isEmpty() || password.isEmpty()) {
            listener.onLoginFailure("Email dan Password tidak boleh kosong.");
            return;
        }

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && mAuth.getCurrentUser() != null) {
                        checkUserRole(mAuth.getCurrentUser().getUid(), listener);
                    } else {
                        String error = task.getException() != null ? task.getException().getMessage() : "Login Gagal";
                        listener.onLoginFailure(error);
                    }
                });
    }

    // --- REGISTER EMAIL BIASA ---
    public void registerUser(String name, String email, String password, String confirmPassword, final RegisterListener listener) {
        if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
            listener.onRegisterFailure("Semua field wajib diisi.");
            return;
        }
        if (password.length() < 6) {
            listener.onRegisterFailure("Password minimal 6 karakter.");
            return;
        }
        if (!password.equals(confirmPassword)) {
            listener.onRegisterFailure("Konfirmasi password tidak cocok.");
            return;
        }

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && mAuth.getCurrentUser() != null) {
                        createUserDocument(mAuth.getCurrentUser().getUid(), name, email, listener);
                    } else {
                        String error = task.getException() != null ? task.getException().getMessage() : "Gagal Register";
                        listener.onRegisterFailure(error);
                    }
                });
    }

    // --- LOGIN & REGISTER VIA GOOGLE (Dual Function) ---
    public void loginWithGoogle(String idToken, final LoginListener listener) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);

        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // Sukses Auth Google, sekarang cek Database
                        FirebaseUser firebaseUser = mAuth.getCurrentUser();
                        checkAndCreateUserInFirestore(firebaseUser, listener);
                    } else {
                        listener.onLoginFailure("Google Auth Error: " + task.getException().getMessage());
                    }
                });
    }

    // === HELPER METHODS ===

    // Cek Role (Untuk Login Biasa)
    private void checkUserRole(String uid, final LoginListener listener) {
        db.collection("users").document(uid).get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult().exists()) {
                String role = task.getResult().getString("role");
                listener.onLoginSuccess(role != null ? role : "user");
            } else {
                listener.onLoginFailure("Data user tidak ditemukan di database.");
            }
        });
    }

    // Simpan Data Baru (Untuk Register Biasa)
    private void createUserDocument(String uid, String name, String email, final RegisterListener listener) {
        Map<String, Object> userMap = new HashMap<>();
        userMap.put("name", name);
        userMap.put("email", email);
        userMap.put("role", "user");

        db.collection("users").document(uid).set(userMap)
                .addOnSuccessListener(a -> listener.onRegisterSuccess())
                .addOnFailureListener(e -> listener.onRegisterFailure(e.getMessage()));
    }

    // Logika Cerdas: Cek Dulu -> Kalau Gak Ada, Buat Baru (Untuk Google)
    private void checkAndCreateUserInFirestore(FirebaseUser user, final LoginListener listener) {
        DocumentReference userRef = db.collection("users").document(user.getUid());

        userRef.get().addOnSuccessListener(document -> {
            if (document.exists()) {
                // KASUS 1: Akun SUDAH ADA -> Langsung Login
                String role = document.getString("role");
                Log.d(TAG, "User Google lama. Login sebagai: " + role);
                listener.onLoginSuccess(role != null ? role : "user");
            } else {
                // KASUS 2: Akun BELUM ADA -> Register Otomatis
                Log.d(TAG, "User Google baru. Membuat data...");
                Map<String, Object> newUser = new HashMap<>();
                newUser.put("name", user.getDisplayName());
                newUser.put("email", user.getEmail());
                newUser.put("role", "user"); // Default user

                // Simpan foto profil Google jika ada
                if (user.getPhotoUrl() != null) {
                    newUser.put("photoUrl", user.getPhotoUrl().toString());
                }

                userRef.set(newUser)
                        .addOnSuccessListener(aVoid -> {
                            Log.d(TAG, "Sukses buat data user baru.");
                            listener.onLoginSuccess("user");
                        })
                        .addOnFailureListener(e -> listener.onLoginFailure("Gagal simpan data user: " + e.getMessage()));
            }
        }).addOnFailureListener(e -> listener.onLoginFailure("Database Error: " + e.getMessage()));
    }
}