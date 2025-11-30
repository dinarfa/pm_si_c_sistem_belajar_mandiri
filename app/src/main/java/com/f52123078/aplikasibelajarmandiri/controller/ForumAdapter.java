package com.f52123078.aplikasibelajarmandiri.controller;

import android.content.Context;
import android.content.Intent;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.f52123078.aplikasibelajarmandiri.R;
import com.f52123078.aplikasibelajarmandiri.databinding.ItemForumPostBinding;
import com.f52123078.aplikasibelajarmandiri.model.ForumPost;
import java.util.ArrayList;
import java.util.List;

public class ForumAdapter extends RecyclerView.Adapter<ForumAdapter.PostViewHolder> {

    private final List<ForumPost> postList;

    public ForumAdapter(List<ForumPost> postList) {
        this.postList = new ArrayList<>(postList);
    }

    public void updateList(List<ForumPost> newList) {
        this.postList.clear();
        this.postList.addAll(newList);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public PostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemForumPostBinding binding = ItemForumPostBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new PostViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull PostViewHolder holder, int position) {
        ForumPost post = postList.get(position);
        holder.bind(post);

        holder.itemView.setOnClickListener(v -> {
            Context context = v.getContext();
            Intent intent = new Intent(context, DetailForumActivity.class);
            intent.putExtra("POST_ID", post.getPostId());
            intent.putExtra("USER_NAME", post.getUserName());
            intent.putExtra("TEXT", post.getText());
            intent.putExtra("IMAGE_URL", post.getImageUrl());
            intent.putExtra("POST_OWNER_ID", post.getUserId());
            intent.putExtra("USER_PHOTO", post.getUserPhotoUrl());
            if (post.getTimestamp() != null) {
                intent.putExtra("TIMESTAMP", post.getTimestamp().toString());
            }
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() { return postList.size(); }

    static class PostViewHolder extends RecyclerView.ViewHolder {
        private final ItemForumPostBinding binding;

        public PostViewHolder(ItemForumPostBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(ForumPost post) {
            binding.tvUserName.setText(post.getUserName());
            binding.tvPostText.setText(post.getText());

            // Tampilkan Label MK
            if (post.getMkName() != null && !post.getMkName().isEmpty()) {
                binding.chipMkLabel.setText(post.getMkName());
                binding.chipMkLabel.setVisibility(View.VISIBLE);
            } else {
                binding.chipMkLabel.setVisibility(View.GONE);
            }

            if (post.getTimestamp() != null) {
                CharSequence timeAgo = DateUtils.getRelativeTimeSpanString(
                        post.getTimestamp().getTime(), System.currentTimeMillis(), DateUtils.MINUTE_IN_MILLIS);
                binding.tvTimestamp.setText(timeAgo);
            } else {
                binding.tvTimestamp.setText("Baru saja");
            }

            if (post.getUserPhotoUrl() != null && !post.getUserPhotoUrl().isEmpty()) {
                Glide.with(itemView.getContext()).load(post.getUserPhotoUrl()).circleCrop().into(binding.ivUserAvatar);
            } else {
                binding.ivUserAvatar.setImageResource(R.drawable.ic_launcher_background);
            }

            if (post.getImageUrl() != null && !post.getImageUrl().isEmpty()) {
                binding.ivPostImage.setVisibility(View.VISIBLE);
                Glide.with(itemView.getContext()).load(post.getImageUrl()).into(binding.ivPostImage);
            } else {
                binding.ivPostImage.setVisibility(View.GONE);
            }
        }
    }
}