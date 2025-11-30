package com.f52123078.aplikasibelajarmandiri;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.f52123078.aplikasibelajarmandiri.databinding.ActivityNotificationBinding;
import com.f52123078.aplikasibelajarmandiri.model.Notification;
import com.f52123078.aplikasibelajarmandiri.controller.NotificationAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import java.util.ArrayList;
import java.util.List;

public class NotificationActivity extends AppCompatActivity {

    private static final String TAG = "NotifActivity"; // Tag untuk Logcat
    private ActivityNotificationBinding binding;
    private NotificationAdapter adapter;
    private List<Notification> notifList;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityNotificationBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        notifList = new ArrayList<>();

        setupToolbar();
        setupRecyclerView();
        loadNotificationsRealtime();
    }

    private void setupToolbar() {
        binding.toolbarNotification.setNavigationOnClickListener(v -> finish());
    }

    private void setupRecyclerView() {
        adapter = new NotificationAdapter(notifList);
        binding.rvNotifications.setLayoutManager(new LinearLayoutManager(this));
        binding.rvNotifications.setAdapter(adapter);
    }

    private void loadNotificationsRealtime() {
        if (mAuth.getCurrentUser() == null) return;

        String myId = mAuth.getCurrentUser().getUid();
        Log.d(TAG, "Mulai mengambil notifikasi untuk User ID: " + myId);

        binding.progressBarNotif.setVisibility(View.VISIBLE);
        binding.tvEmptyNotif.setVisibility(View.GONE);

        // Menggunakan Realtime Listener (addSnapshotListener)
        db.collection("notifications")
                .whereEqualTo("userId", myId)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .addSnapshotListener((value, error) -> {
                    // 1. Matikan Loading APAPUN HASILNYA
                    binding.progressBarNotif.setVisibility(View.GONE);

                    if (error != null) {
                        Log.e(TAG, "Error ambil notifikasi: ", error);
                        Toast.makeText(this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (value != null) {
                        Log.d(TAG, "Data ditemukan: " + value.size() + " notifikasi");
                        notifList.clear();
                        notifList.addAll(value.toObjects(Notification.class));
                        adapter.notifyDataSetChanged();

                        if (notifList.isEmpty()) {
                            binding.tvEmptyNotif.setVisibility(View.VISIBLE);
                            binding.rvNotifications.setVisibility(View.GONE);
                        } else {
                            binding.tvEmptyNotif.setVisibility(View.GONE);
                            binding.rvNotifications.setVisibility(View.VISIBLE);
                        }
                    }
                });
    }
}