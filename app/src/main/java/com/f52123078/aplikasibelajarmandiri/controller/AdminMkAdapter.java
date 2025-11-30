package com.f52123078.aplikasibelajarmandiri.controller;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.f52123078.aplikasibelajarmandiri.databinding.ItemMkAdminBinding;
import com.f52123078.aplikasibelajarmandiri.model.MataKuliah;
import java.util.List;

public class AdminMkAdapter extends RecyclerView.Adapter<AdminMkAdapter.ViewHolder> {

    public interface OnActionClickListener {
        void onEdit(MataKuliah mk);
        void onDelete(MataKuliah mk);
    }

    private List<MataKuliah> list;
    private final OnActionClickListener listener;

    public AdminMkAdapter(List<MataKuliah> list, OnActionClickListener listener) {
        this.list = list;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(ItemMkAdminBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(list.get(position));
    }

    @Override
    public int getItemCount() { return list.size(); }

    class ViewHolder extends RecyclerView.ViewHolder {
        ItemMkAdminBinding binding;
        ViewHolder(ItemMkAdminBinding b) { super(b.getRoot()); binding = b; }

        void bind(MataKuliah mk) {
            binding.tvMkName.setText(mk.getName());
            binding.tvProdiName.setText(mk.getProdiName()); // Pastikan field ini ada di Firestore
            binding.btnEdit.setOnClickListener(v -> listener.onEdit(mk));
            binding.btnDelete.setOnClickListener(v -> listener.onDelete(mk));
        }
    }
}