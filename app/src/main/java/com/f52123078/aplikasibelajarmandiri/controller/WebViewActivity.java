package com.f52123078.aplikasibelajarmandiri.controller; // Sesuaikan package

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings; // Import ini
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.appcompat.app.AppCompatActivity;

import com.f52123078.aplikasibelajarmandiri.databinding.ActivityWebViewBinding;

public class WebViewActivity extends AppCompatActivity {

    private ActivityWebViewBinding binding;
    public static final String EXTRA_URL = "EXTRA_URL";
    public static final String EXTRA_TITLE = "EXTRA_TITLE";

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityWebViewBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Ambil data dari Intent
        String url = getIntent().getStringExtra(EXTRA_URL);
        String title = getIntent().getStringExtra(EXTRA_TITLE);

        // Setup Toolbar
        setSupportActionBar(binding.toolbarWebview);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(title != null ? title : "Loading...");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        binding.toolbarWebview.setNavigationOnClickListener(v -> finish());

        // --- SETUP WEBVIEW (PENTING) ---
        WebSettings settings = binding.webView.getSettings();

        // 1. Wajib untuk website modern
        settings.setJavaScriptEnabled(true);

        // 2. SOLUSI UTAMA: Aktifkan DOM Storage (Agar CodeDex dll bisa jalan)
        settings.setDomStorageEnabled(true);

        // 3. Tambahan untuk kompatibilitas lebih baik
        settings.setDatabaseEnabled(true);
        settings.setLoadWithOverviewMode(true);
        settings.setUseWideViewPort(true);
        settings.setBuiltInZoomControls(true);
        settings.setDisplayZoomControls(false); // Sembunyikan tombol zoom +/-

        // Set WebViewClient agar link tetap terbuka di dalam aplikasi
        binding.webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                binding.progressBarWeb.setVisibility(View.VISIBLE);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                binding.progressBarWeb.setVisibility(View.GONE);

                // Update judul toolbar jika judul awal kosong
                if (title == null || title.isEmpty()) {
                    if (getSupportActionBar() != null) {
                        getSupportActionBar().setTitle(view.getTitle());
                    }
                }
            }
        });

        // Set WebChromeClient untuk handle progress bar
        binding.webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                super.onProgressChanged(view, newProgress);
                if (newProgress < 100 && binding.progressBarWeb.getVisibility() == View.GONE) {
                    binding.progressBarWeb.setVisibility(View.VISIBLE);
                }
                if (newProgress == 100) {
                    binding.progressBarWeb.setVisibility(View.GONE);
                }
            }
        });

        // Muat URL
        if (url != null) {
            binding.webView.loadUrl(url);
        }
    }

    @Override
    public void onBackPressed() {
        if (binding.webView.canGoBack()) {
            binding.webView.goBack();
        } else {
            super.onBackPressed();
        }
    }
}