package com.f52123078.aplikasibelajarmandiri.controller;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.f52123078.aplikasibelajarmandiri.R;
import com.f52123078.aplikasibelajarmandiri.model.Notification;
import java.util.List;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.NotifViewHolder> {

    private List<Notification> notifList;

    public NotificationAdapter(List<Notification> notifList) {
        this.notifList = notifList;
    }

    @NonNull
    @Override
    public NotifViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_notification, parent, false);
        return new NotifViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NotifViewHolder holder, int position) {
        Notification notif = notifList.get(position);
        holder.tvTitle.setText(notif.getSenderName() + " membalas diskusi Anda");
        holder.tvMsg.setText(notif.getMessage());

        // --- TAMBAHAN: KLIK NOTIFIKASI ---
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(v.getContext(), DetailForumActivity.class);
            // Kita hanya punya Post ID di sini
            intent.putExtra("POST_ID", notif.getPostId());
            // Beri tanda kalau ini datang dari notifikasi
            intent.putExtra("FROM_NOTIF", true);

            v.getContext().startActivity(intent);
        });
    }

    @Override
    public int getItemCount() { return notifList.size(); }

    static class NotifViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvMsg;
        public NotifViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tv_notif_title);
            tvMsg = itemView.findViewById(R.id.tv_notif_msg);
        }
    }
}