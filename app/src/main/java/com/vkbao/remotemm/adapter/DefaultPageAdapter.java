package com.vkbao.remotemm.adapter;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.vkbao.remotemm.databinding.DefaultPageItemBinding;
import com.vkbao.remotemm.helper.Helper;

import java.util.List;

public class DefaultPageAdapter extends RecyclerView.Adapter<DefaultPageAdapter.DefaultPageViewHolder> {
    private List<String> moduleList;
    private Context context;
    private OnItemClick<String> listener;

    public DefaultPageAdapter(List<String> moduleList, OnItemClick<String> listener) {
        this.moduleList = moduleList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public DefaultPageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        DefaultPageItemBinding binding = DefaultPageItemBinding.inflate(LayoutInflater.from(context), parent, false);
        return new DefaultPageViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull DefaultPageViewHolder holder, int position) {
        String module = moduleList.get(position);

        holder.binding.name.setText(module);
        holder.binding.deleteBtn.setOnClickListener(view -> {
            if (listener != null)   {
                listener.onDeleteClick(module);
            }
        });
    }

    @Override
    public int getItemCount() {
        return moduleList.size();
    }

    public class DefaultPageViewHolder extends RecyclerView.ViewHolder {
        private DefaultPageItemBinding binding;

        public DefaultPageViewHolder(@NonNull DefaultPageItemBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }

    public interface OnItemClick<T> {
        void onDeleteClick(T value);
    }
}
