package com.vkbao.remotemm.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.vkbao.remotemm.databinding.FileItemBinding;
import com.vkbao.remotemm.databinding.ModuleItemBinding;
import com.vkbao.remotemm.helper.Helper;

import java.util.List;

public class BackupFileAdapter extends RecyclerView.Adapter<BackupFileAdapter.BackupFileViewHolder> {
    private List<String> files;
    private OnClickItem<String> listener;

    public BackupFileAdapter(List<String> files, OnClickItem<String> listener) {
        this.files = files;
        this.listener = listener;
    }

    @NonNull
    @Override
    public BackupFileViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        FileItemBinding binding = FileItemBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new BackupFileViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull BackupFileViewHolder holder, int position) {
        holder.setFileName(files.get(position));
        if (listener != null) {
            holder.binding.getRoot().setOnClickListener(view -> {
                listener.setOnClickItem(view, files.get(position));
            });
        }
    }

    @Override
    public int getItemCount() {
        return files.size();
    }

    public class BackupFileViewHolder extends RecyclerView.ViewHolder {
        private FileItemBinding binding;

        public BackupFileViewHolder(@NonNull FileItemBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void setFileName(String fileName) {
            binding.name.setText(Helper.unixTime2Date(fileName));
        }
    }

    public interface OnClickItem<T> {
        public void setOnClickItem(View view, T item);
    }
}
