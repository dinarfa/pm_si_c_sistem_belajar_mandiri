package com.f52123078.aplikasibelajarmandiri.controller;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.f52123078.aplikasibelajarmandiri.R;
import com.f52123078.aplikasibelajarmandiri.model.Resource;
import com.f52123078.aplikasibelajarmandiri.model.UserActivityModel; // Import Model History

import java.util.ArrayList;
import java.util.List;

public class ResourceAdapter extends RecyclerView.Adapter<ResourceAdapter.ResourceViewHolder> {

    private List<Resource> resourceList;
    private List<String> documentIds; // Tambahan untuk menyimpan ID Dokumen
    private Context context;
    private UserActivityModel userActivityModel; // Model untuk simpan history

    // --- KONSTRUKTOR 2 PARAMETER (Sesuai Patokan Lama) ---
    public ResourceAdapter(List<Resource> resourceList, List<String> documentIds) {
        this.resourceList = (resourceList != null) ? resourceList : new ArrayList<>();
        this.documentIds = (documentIds != null) ? documentIds : new ArrayList<>();
        this.userActivityModel = new UserActivityModel();
    }

    // Konstruktor 1 parameter (Opsional, untuk kompatibilitas Search/Browse)
    public ResourceAdapter(List<Resource> resourceList) {
        this(resourceList, new ArrayList<>());
    }

    // Update list untuk fitur search
    public void updateList(List<Resource> newList) {
        this.resourceList = (newList != null) ? newList : new ArrayList<>();
        // Catatan: Saat search, documentIds mungkin tidak sinkron jika tidak diupdate juga.
        // Namun untuk tampilan saja ini sudah cukup.
        notifyDataSetChanged();
    }

    // Update list lengkap dengan ID (Overload)
    public void updateList(List<Resource> newList, List<String> newIds) {
        this.resourceList = (newList != null) ? newList : new ArrayList<>();
        this.documentIds = (newIds != null) ? newIds : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ResourceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        // MENGGUNAKAN LAYOUT BARU YANG KEREN (item_resource_card.xml)
        View view = LayoutInflater.from(context).inflate(R.layout.item_resource_card, parent, false);
        return new ResourceViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ResourceViewHolder holder, int position) {
        Resource resource = resourceList.get(position);

        // Ambil Doc ID jika ada (untuk history)
        String docId = "";
        if (documentIds != null && position < documentIds.size()) {
            docId = documentIds.get(position);
        }

        // 1. SET TAMPILAN (Layout Baru)
        holder.tvTitle.setText(resource.getTitle());

        // Format Subtitle: Nama MK • Tipe File
        String typeLabel = (resource.getType() != null) ? resource.getType().toUpperCase() : "FILE";
        String mkName = (resource.getCourse() != null) ? resource.getCourse() : "Umum";
        holder.tvSubtitle.setText(String.format("%s • %s", mkName, typeLabel));

        // 2. SET IKON DINAMIS
        setDynamicIcon(holder.ivIcon, resource.getType());

        // 3. HANDLE KLIK (Menggunakan Logika Patokan Anda)
        String finalDocId = docId;
        holder.itemView.setOnClickListener(v -> {

            // A. Simpan History (UserActivityModel)
            if (!finalDocId.isEmpty()) {
                userActivityModel.saveLastAccessedResource(
                        finalDocId,
                        resource.getTitle(),
                        resource.getDesc(),
                        resource.getUrl()
                );
            }

            // B. Buka URL (Logika Patokan)
            if (resource.getUrl() != null && !resource.getUrl().isEmpty()) {
                String urlToLoad = resource.getUrl();
                String type = resource.getType();
                String lowerUrl = urlToLoad.toLowerCase();

                // --- LOGIKA URL (PATOKAN) ---

                // 1. Prioritas Google Drive (JANGAN DIUBAH, biarkan WebView handle)
                if (lowerUrl.contains("drive.google.com")) {
                    // Pass (tidak diubah)
                }
                // 2. Cek Tipe dari Database
                else if (type != null) {
                    if (type.equalsIgnoreCase("pdf")) {
                        // PDF Langsung -> Bungkus dengan GView
                        urlToLoad = "https://docs.google.com/gview?embedded=true&url=" + urlToLoad;
                    } else if (type.equalsIgnoreCase("youtube")) {
                        // YouTube -> Embed
                        if (urlToLoad.contains("/watch?v=")) {
                            try {
                                String videoId = urlToLoad.split("v=")[1].split("&")[0];
                                urlToLoad = "https://www.youtube.com/embed/" + videoId;
                            } catch (Exception e) { /* Ignore */ }
                        }
                    }
                }
                // 3. Fallback (Cek String URL)
                else if (lowerUrl.endsWith(".pdf")) {
                    urlToLoad = "https://docs.google.com/gview?embedded=true&url=" + urlToLoad;
                } else if (lowerUrl.contains("youtube.com/watch?v=")) {
                    try {
                        String videoId = urlToLoad.split("v=")[1].split("&")[0];
                        urlToLoad = "https://www.youtube.com/embed/" + videoId;
                    } catch (Exception e) { /* Ignore */ }
                }

                // Buka WebViewActivity
                Intent intent = new Intent(context, WebViewActivity.class);
                intent.putExtra(WebViewActivity.EXTRA_URL, urlToLoad);
                intent.putExtra(WebViewActivity.EXTRA_TITLE, resource.getTitle());
                context.startActivity(intent);

            } else {
                Toast.makeText(context, "Link resource tidak valid", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Helper Ikon
    private void setDynamicIcon(ImageView imageView, String type) {
        int iconResId;
        if (type == null) {
            iconResId = R.drawable.ic_document;
        } else {
            String lowerType = type.toLowerCase();
            if (lowerType.contains("pdf")) {
                iconResId = R.drawable.pdf;
            } else if (lowerType.contains("video") || lowerType.contains("youtube")) {
                iconResId = R.drawable.play;
            } else if (lowerType.contains("link") || lowerType.contains("web") || lowerType.contains("website")) {
                iconResId = R.drawable.domain;
            } else {
                iconResId = R.drawable.ic_document;
            }
        }
        imageView.setImageResource(iconResId);
    }

    @Override
    public int getItemCount() {
        return resourceList.size();
    }

    // ViewHolder untuk Layout Baru
    public static class ResourceViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvSubtitle;
        ImageView ivIcon;

        public ResourceViewHolder(@NonNull View itemView) {
            super(itemView);
            // Binding manual ke ID di item_resource_card.xml
            tvTitle = itemView.findViewById(R.id.tv_resource_title);
            tvSubtitle = itemView.findViewById(R.id.tv_resource_subtitle);
            ivIcon = itemView.findViewById(R.id.iv_resource_icon);
        }
    }
}