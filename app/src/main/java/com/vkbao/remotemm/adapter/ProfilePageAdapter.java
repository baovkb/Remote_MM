package com.vkbao.remotemm.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.vkbao.remotemm.R;
import com.vkbao.remotemm.databinding.ProfilePageItemBinding;

import java.util.List;

public class ProfilePageAdapter extends RecyclerView.Adapter<ProfilePageAdapter.ProfilePageViewHolder> {
    private List<List<String>> pages;
    private Context context;
    private OnPageEvent<Integer, String> listener;

    public ProfilePageAdapter(List<List<String>> pages, OnPageEvent<Integer, String> listener) {
        this.pages = pages;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ProfilePageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        ProfilePageItemBinding binding = ProfilePageItemBinding.inflate(LayoutInflater.from(context), parent, false);
        return new ProfilePageViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ProfilePageViewHolder holder, int position) {
        List<String> modules = pages.get(position);
        String title = String.format(context.getString(R.string.profile_page), String.valueOf(position + 1), String.valueOf(pages.size()));
        holder.binding.title.setText(title);

        DefaultPageAdapter defaultPageAdapter = new DefaultPageAdapter(modules, module -> {
            listener.onDeleteClick(holder.getAdapterPosition(), module);
        });
        holder.binding.moduleList.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false));
        holder.binding.moduleList.setAdapter(defaultPageAdapter);

        holder.binding.addBtn.setOnClickListener(view -> {
            listener.onAddClick(holder.getAdapterPosition());
        });
    }

    @Override
    public int getItemCount() {
        return pages.size();
    }


    public class ProfilePageViewHolder extends RecyclerView.ViewHolder {
        private ProfilePageItemBinding binding;

        public ProfilePageViewHolder(@NonNull ProfilePageItemBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }

    public interface OnPageEvent<P, T> {
        void onAddClick(P page);
        void onDeleteClick(P page, T module);
    }
}
