package com.f52123078.aplikasibelajarmandiri.controller;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.f52123078.aplikasibelajarmandiri.databinding.ItemResourceAdminBinding;
import com.f52123078.aplikasibelajarmandiri.model.Resource;

import java.util.List;

public class AdminResourceAdapter extends RecyclerView.Adapter<AdminResourceAdapter.AdminViewHolder> {

    // Interface untuk mengirim klik kembali ke Activity (Controller)
    public interface OnResourceActionClicked {
        void onEditClicked(Resource resource, String documentId);
        void onDeleteClicked(String documentId, String resourceTitle);
    }

    private final List<Resource> resourceList;
    private final List<String> documentIds;
    private final OnResourceActionClicked listener;

    public AdminResourceAdapter(List<Resource> resourceList, List<String> documentIds, OnResourceActionClicked listener) {
        this.resourceList = resourceList;
        this.documentIds = documentIds;
        this.listener = listener;
    }

    @NonNull
    @Override
    public AdminViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemResourceAdminBinding binding = ItemResourceAdminBinding.inflate(
                LayoutInflater.from(parent.getContext()),
                parent,
                false
        );
        return new AdminViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull AdminViewHolder holder, int position) {
        Resource resource = resourceList.get(position);
        String docId = documentIds.get(position);
        holder.bind(resource, docId, listener);
    }

    @Override
    public int getItemCount() {
        return resourceList.size();
    }

    static class AdminViewHolder extends RecyclerView.ViewHolder {
        private final ItemResourceAdminBinding binding;

        public AdminViewHolder(@NonNull ItemResourceAdminBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(final Resource resource, final String docId, final OnResourceActionClicked listener) {
            binding.tvAdminResourceTitle.setText(resource.getTitle());
            String subtitle = (resource.getCourse() != null ? resource.getCourse() : "-") +
                    " • " +
                    (resource.getCategory() != null ? resource.getCategory() : "-");
            binding.tvAdminResourceSubtitle.setText(subtitle);

            // Kirim data kembali ke Activity (Controller) saat diklik
            binding.btnAdminEdit.setOnClickListener(v ->
                    listener.onEditClicked(resource, docId));

            binding.btnAdminDelete.setOnClickListener(v ->
                    listener.onDeleteClicked(docId, resource.getTitle()));
        }
    }
}

