package com.f52123078.aplikasibelajarmandiri.controller;

import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.bumptech.glide.Glide;
import com.f52123078.aplikasibelajarmandiri.R; // Pastikan Import R
import com.f52123078.aplikasibelajarmandiri.databinding.ActivityDetailForumBinding;
import com.f52123078.aplikasibelajarmandiri.model.ForumPost;
import com.f52123078.aplikasibelajarmandiri.model.ForumReply;
import com.f52123078.aplikasibelajarmandiri.model.Notification;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import java.util.ArrayList;
import java.util.List;

public class DetailForumActivity extends AppCompatActivity implements ForumReplyAdapter.OnReplyActionListener {

    private ActivityDetailForumBinding binding;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private String postId;
    private ForumReplyAdapter adapter;
    private List<ForumReply> replyList;
    private String postOwnerId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityDetailForumBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        replyList = new ArrayList<>();

        setupRecyclerView();

        binding.toolbarDetail.setNavigationOnClickListener(v -> finish());
        binding.btnSendReply.setOnClickListener(v -> sendReply());

        // --- LOGIKA DATA ---
        postId = getIntent().getStringExtra("POST_ID");
        boolean isFromNotif = getIntent().getBooleanExtra("FROM_NOTIF", false);

        if (postId == null) {
            Toast.makeText(this, "Error: Post ID hilang", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        if (isFromNotif) {
            loadPostDataFromFirestore(postId);
        } else {
            // Ambil data lengkap dari Intent
            String name = getIntent().getStringExtra("USER_NAME");
            String text = getIntent().getStringExtra("TEXT");
            String imageUrl = getIntent().getStringExtra("IMAGE_URL");
            String userPhoto = getIntent().getStringExtra("USER_PHOTO"); // Foto Profil
            String timestamp = getIntent().getStringExtra("TIMESTAMP");
            postOwnerId = getIntent().getStringExtra("POST_OWNER_ID");

            setupViews(name, text, imageUrl, userPhoto, timestamp);
            checkOwnerPermission(); // Cek apakah saya pemilik postingan?
            loadReplies();
        }
    }

    // --- LOGIKA MENU EDIT/HAPUS POSTINGAN ---
    private void checkOwnerPermission() {
        if (mAuth.getCurrentUser() != null && postOwnerId != null) {
            if (mAuth.getCurrentUser().getUid().equals(postOwnerId)) {
                // Saya pemilik postingan -> Munculkan tombol opsi
                binding.btnMoreOptions.setVisibility(View.VISIBLE);
                binding.btnMoreOptions.setOnClickListener(v -> showPostOptions());
            } else {
                binding.btnMoreOptions.setVisibility(View.GONE);
            }
        }
    }

    private void showPostOptions() {
        String[] options = {"Edit Postingan", "Hapus Postingan"};
        new AlertDialog.Builder(this)
                .setTitle("Pilih Aksi")
                .setItems(options, (dialog, which) -> {
                    if (which == 0) {
                        editPostDialog();
                    } else {
                        deletePostDialog();
                    }
                })
                .show();
    }

    private void editPostDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Edit Postingan");

        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
        input.setText(binding.tvDetailText.getText().toString()); // Isi teks lama
        input.setPadding(40, 40, 40, 40);
        builder.setView(input);

        builder.setPositiveButton("Simpan", (dialog, which) -> {
            String newText = input.getText().toString().trim();
            if (!newText.isEmpty()) {
                db.collection("forum_posts").document(postId).update("text", newText)
                        .addOnSuccessListener(a -> {
                            binding.tvDetailText.setText(newText);
                            Toast.makeText(this, "Postingan diperbarui", Toast.LENGTH_SHORT).show();
                        });
            }
        });
        builder.setNegativeButton("Batal", null);
        builder.show();
    }

    private void deletePostDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Hapus Postingan")
                .setMessage("Yakin ingin menghapus diskusi ini selamanya?")
                .setPositiveButton("Hapus", (dialog, which) -> {
                    db.collection("forum_posts").document(postId).delete()
                            .addOnSuccessListener(a -> {
                                Toast.makeText(this, "Postingan dihapus", Toast.LENGTH_SHORT).show();
                                finish(); // Tutup halaman dan kembali
                            })
                            .addOnFailureListener(e -> Toast.makeText(this, "Gagal: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                })
                .setNegativeButton("Batal", null)
                .show();
    }

    // --- LOGIKA VIEW ---

    private void loadPostDataFromFirestore(String targetPostId) {
        db.collection("forum_posts").document(targetPostId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        ForumPost post = documentSnapshot.toObject(ForumPost.class);
                        if (post != null) {
                            setupViews(
                                    post.getUserName(),
                                    post.getText(),
                                    post.getImageUrl(),
                                    post.getUserPhotoUrl(), // Ambil foto profil
                                    post.getTimestamp() != null ? post.getTimestamp().toString() : ""
                            );
                            postOwnerId = post.getUserId();
                            checkOwnerPermission(); // Cek lagi setelah data didapat
                            loadReplies();
                        }
                    } else {
                        Toast.makeText(this, "Postingan ini sudah dihapus", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                });
    }

    private void setupViews(String name, String text, String imageUrl, String userPhoto, String time) {
        binding.tvDetailName.setText(name);
        binding.tvDetailText.setText(text);
        binding.tvDetailTime.setText(time != null ? time : "");

        // Load Foto Profil (Avatar)
        if (userPhoto != null && !userPhoto.isEmpty()) {
            Glide.with(this).load(userPhoto).circleCrop().into(binding.ivDetailAvatar);
        } else {
            binding.ivDetailAvatar.setImageResource(R.drawable.ic_launcher_background);
        }

        // Load Foto Postingan (Lampiran)
        if (imageUrl != null && !imageUrl.isEmpty()) {
            binding.ivDetailImage.setVisibility(View.VISIBLE);
            Glide.with(this).load(imageUrl).into(binding.ivDetailImage);
        } else {
            binding.ivDetailImage.setVisibility(View.GONE);
        }
    }

    // --- KODE ADAPTER & REPLY ---

    private void setupRecyclerView() {
        String myUid = (mAuth.getCurrentUser() != null) ? mAuth.getCurrentUser().getUid() : "";
        adapter = new ForumReplyAdapter(replyList, myUid, this);
        binding.rvReplies.setLayoutManager(new LinearLayoutManager(this));
        binding.rvReplies.setAdapter(adapter);
        binding.rvReplies.setNestedScrollingEnabled(false);
    }

    private void loadReplies() {
        db.collection("forum_posts").document(postId).collection("replies")
                .orderBy("timestamp", Query.Direction.ASCENDING)
                .addSnapshotListener((value, error) -> {
                    if (error != null) return;
                    if (value != null) {
                        replyList.clear();
                        for (DocumentSnapshot doc : value.getDocuments()) {
                            ForumReply reply = doc.toObject(ForumReply.class);
                            if (reply != null) {
                                reply.setReplyId(doc.getId());
                                replyList.add(reply);
                            }
                        }
                        adapter.notifyDataSetChanged();
                        if (replyList.isEmpty()) {
                            binding.tvNoReplies.setVisibility(View.VISIBLE);
                            binding.rvReplies.setVisibility(View.GONE);
                        } else {
                            binding.tvNoReplies.setVisibility(View.GONE);
                            binding.rvReplies.setVisibility(View.VISIBLE);
                        }
                    }
                });
    }

    // Interface Edit Komentar
    @Override
    public void onEdit(ForumReply reply) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Edit Komentar");
        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
        input.setText(reply.getText());
        input.setPadding(32, 32, 32, 32);
        builder.setView(input);
        builder.setPositiveButton("Simpan", (d, w) -> {
            String newText = input.getText().toString().trim();
            if(!newText.isEmpty()) updateReplyInFirestore(reply.getReplyId(), newText);
        });
        builder.setNegativeButton("Batal", null);
        builder.show();
    }

    // Interface Hapus Komentar
    @Override
    public void onDelete(String replyId) {
        new AlertDialog.Builder(this)
                .setTitle("Hapus Komentar")
                .setMessage("Hapus komentar ini?")
                .setPositiveButton("Hapus", (d, w) -> deleteReplyInFirestore(replyId))
                .setNegativeButton("Batal", null)
                .show();
    }

    private void updateReplyInFirestore(String replyId, String newText) {
        db.collection("forum_posts").document(postId)
                .collection("replies").document(replyId)
                .update("text", newText)
                .addOnSuccessListener(a -> Toast.makeText(this, "Komentar diperbarui", Toast.LENGTH_SHORT).show());
    }

    private void deleteReplyInFirestore(String replyId) {
        db.collection("forum_posts").document(postId)
                .collection("replies").document(replyId)
                .delete()
                .addOnSuccessListener(a -> Toast.makeText(this, "Komentar dihapus", Toast.LENGTH_SHORT).show());
    }

    private void sendReply() {
        String replyText = binding.etReplyInput.getText().toString().trim();
        if (replyText.isEmpty()) return;
        if (mAuth.getCurrentUser() == null) return;
        binding.btnSendReply.setEnabled(false);

        db.collection("users").document(mAuth.getCurrentUser().getUid()).get()
                .addOnSuccessListener(doc -> {
                    String myName = doc.getString("name");
                    if (myName == null) myName = "User";
                    ForumReply reply = new ForumReply(mAuth.getCurrentUser().getUid(), myName, replyText);
                    db.collection("forum_posts").document(postId).collection("replies").add(reply)
                            .addOnSuccessListener(d -> {
                                binding.etReplyInput.setText("");
                                binding.btnSendReply.setEnabled(true);
                                Toast.makeText(this, "Terkirim", Toast.LENGTH_SHORT).show();
                                createNotification(replyText);
                            });
                });
    }

    private void createNotification(String replyContent) {
        if (mAuth.getCurrentUser() == null || postOwnerId == null) return;
        if (mAuth.getCurrentUser().getUid().equals(postOwnerId)) return;
        db.collection("users").document(mAuth.getCurrentUser().getUid()).get()
                .addOnSuccessListener(doc -> {
                    String myName = doc.getString("name");
                    if (myName == null) myName = "Seseorang";
                    Notification notif = new Notification(postOwnerId, myName, "membalas: " + replyContent, postId);
                    db.collection("notifications").add(notif);
                });
    }
}