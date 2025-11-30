package com.f52123078.aplikasibelajarmandiri.controller;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.f52123078.aplikasibelajarmandiri.databinding.ItemProdiAdminBinding;
import com.f52123078.aplikasibelajarmandiri.model.Prodi;
import java.util.List;

public class AdminProdiAdapter extends RecyclerView.Adapter<AdminProdiAdapter.ViewHolder> {

    public interface OnActionClickListener {
        void onEdit(Prodi prodi);
        void onDelete(Prodi prodi);
    }

    private List<Prodi> list;
    private final OnActionClickListener listener;

    public AdminProdiAdapter(List<Prodi> list, OnActionClickListener listener) {
        this.list = list;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(ItemProdiAdminBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(list.get(position));
    }

    @Override
    public int getItemCount() { return list.size(); }

    class ViewHolder extends RecyclerView.ViewHolder {
        ItemProdiAdminBinding binding;
        ViewHolder(ItemProdiAdminBinding b) { super(b.getRoot()); binding = b; }

        void bind(Prodi p) {
            binding.tvProdiName.setText(p.getName());
            binding.btnEdit.setOnClickListener(v -> listener.onEdit(p));
            binding.btnDelete.setOnClickListener(v -> listener.onDelete(p));
        }
    }
}