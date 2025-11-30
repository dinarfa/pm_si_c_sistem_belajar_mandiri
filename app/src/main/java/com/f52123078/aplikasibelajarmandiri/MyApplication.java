package com.f52123078.aplikasibelajarmandiri;

import android.app.Application;
import android.util.Log;

import com.cloudinary.android.MediaManager;

import java.util.HashMap;
import java.util.Map;

public class MyApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        // Konfigurasi Cloudinary
        Map<String, String> config = new HashMap<>();

        // --- GANTI 3 BARIS INI DENGAN DATA DARI DASHBOARD CLOUDINARY ANDA ---
        config.put("cloud_name", "dhcjgclpa");
        config.put("api_key", "712568858419822");
        config.put("api_secret", "cGa73hmafSqk9kKBROCi2-ncZ68");
        // ---------------------------------------------------------------------

        config.put("secure", "true");

        try {
            // Inisialisasi MediaManager
            MediaManager.init(this, config);
            Log.d("Cloudinary", "Berhasil inisialisasi Cloudinary");
        } catch (Exception e) {
            // Menangkap error jika MediaManager sudah terinisialisasi sebelumnya
            Log.e("Cloudinary", "Gagal atau sudah inisialisasi: " + e.getMessage());
        }
    }
}