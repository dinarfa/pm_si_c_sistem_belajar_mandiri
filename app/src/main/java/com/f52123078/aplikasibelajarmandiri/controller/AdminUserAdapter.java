package com.f52123078.aplikasibelajarmandiri.viewModel;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.f52123078.aplikasibelajarmandiri.R;
import com.f52123078.aplikasibelajarmandiri.databinding.ItemUserAdminBinding;
import com.f52123078.aplikasibelajarmandiri.model.User;
import java.util.List;

public class AdminUserAdapter extends RecyclerView.Adapter<AdminUserAdapter.UserViewHolder> {

    private final List<User> userList;

    public AdminUserAdapter(List<User> userList) {
        this.userList = userList;
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new UserViewHolder(
                ItemUserAdminBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false)
        );
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        holder.bind(userList.get(position));
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    static class UserViewHolder extends RecyclerView.ViewHolder {
        private final ItemUserAdminBinding binding;

        public UserViewHolder(ItemUserAdminBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(User user) {
            binding.tvUserName.setText(user.getName());
            binding.tvUserEmail.setText(user.getEmail());

            // Set Role Badge
            String role = user.getRole() != null ? user.getRole() : "user";
            binding.tvUserRole.setText(role.toUpperCase());

            // Ubah warna badge jika admin
            if ("admin".equalsIgnoreCase(role)) {
                binding.cardRole.setCardBackgroundColor(itemView.getContext().getColor(android.R.color.holo_red_light));
            } else {
                binding.cardRole.setCardBackgroundColor(itemView.getContext().getColor(android.R.color.darker_gray));
            }

            // Load Foto Profil (Cek apakah User punya method getPhotoUrl, jika belum tambahkan di Model User)
            // Asumsi: Model User.java Anda belum punya getPhotoUrl, jadi kita pakai default dulu
            // Jika sudah ada, uncomment baris bawah:
            /*
            if (user.getPhotoUrl() != null && !user.getPhotoUrl().isEmpty()) {
                Glide.with(itemView.getContext())
                     .load(user.getPhotoUrl())
                     .circleCrop()
                     .into(binding.ivUserAvatar);
            } else {
                binding.ivUserAvatar.setImageResource(R.drawable.ic_launcher_background); // Ganti icon person
            }
            */
            // Default placeholder
            binding.ivUserAvatar.setImageResource(android.R.drawable.sym_def_app_icon);
            // Sebaiknya ganti dengan R.drawable.ic_person jika ada
        }
    }
}