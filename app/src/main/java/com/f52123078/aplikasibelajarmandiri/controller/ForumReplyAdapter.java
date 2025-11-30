package com.f52123078.aplikasibelajarmandiri.controller;

import android.app.AlertDialog;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.f52123078.aplikasibelajarmandiri.databinding.ItemForumReplyBinding;
import com.f52123078.aplikasibelajarmandiri.model.ForumReply;
import java.util.List;

public class ForumReplyAdapter extends RecyclerView.Adapter<ForumReplyAdapter.ReplyViewHolder> {

    private final List<ForumReply> replyList;
    private final String currentUserId; // ID User yang sedang login
    private final OnReplyActionListener listener; // Listener untuk callback

    // Interface untuk komunikasi ke Activity
    public interface OnReplyActionListener {
        void onEdit(ForumReply reply);
        void onDelete(String replyId);
    }

    public ForumReplyAdapter(List<ForumReply> replyList, String currentUserId, OnReplyActionListener listener) {
        this.replyList = replyList;
        this.currentUserId = currentUserId;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ReplyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemForumReplyBinding binding = ItemForumReplyBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new ReplyViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ReplyViewHolder holder, int position) {
        ForumReply reply = replyList.get(position);
        holder.bind(reply, currentUserId, listener);
    }

    @Override
    public int getItemCount() {
        return replyList.size();
    }

    static class ReplyViewHolder extends RecyclerView.ViewHolder {
        private final ItemForumReplyBinding binding;

        public ReplyViewHolder(ItemForumReplyBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(ForumReply reply, String currentUserId, OnReplyActionListener listener) {
            binding.tvReplyUser.setText(reply.getUserName());
            binding.tvReplyText.setText(reply.getText());

            if (reply.getTimestamp() != null) {
                CharSequence timeAgo = DateUtils.getRelativeTimeSpanString(
                        reply.getTimestamp().getTime(),
                        System.currentTimeMillis(),
                        DateUtils.MINUTE_IN_MILLIS);
                binding.tvReplyTime.setText(timeAgo);
            }

            // --- LOGIKA TEKAN LAMA (LONG PRESS) ---
            // Cek apakah yang login adalah pemilik komentar ini?
            if (currentUserId != null && currentUserId.equals(reply.getUserId())) {

                itemView.setOnLongClickListener(v -> {
                    // Tampilkan Dialog Pilihan
                    new AlertDialog.Builder(v.getContext())
                            .setTitle("Pilih Aksi")
                            .setItems(new String[]{"Edit Komentar", "Hapus Komentar"}, (dialog, which) -> {
                                if (which == 0) {
                                    // Klik Edit
                                    listener.onEdit(reply);
                                } else {
                                    // Klik Hapus
                                    listener.onDelete(reply.getReplyId());
                                }
                            })
                            .show();
                    return true; // True artinya event long press sudah ditangani
                });
            } else {
                // Jika bukan pemilik, matikan long click (atau kosongkan)
                itemView.setOnLongClickListener(null);
            }
        }
    }
}