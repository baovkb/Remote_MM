package com.vkbao.remotemm.adapter;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.vkbao.remotemm.R;
import com.vkbao.remotemm.databinding.DirectoryItemBinding;
import com.vkbao.remotemm.helper.Helper;
import com.vkbao.remotemm.model.FileInfo;

import java.util.List;

public class FolderFaceAdapter extends RecyclerView.Adapter<FolderFaceAdapter.FolderFaceViewHolder> {
    private List<FileInfo> fileInfoList;
    private OnItemClick<FileInfo> onItemClick;
    private OnEllipseClick<FileInfo> onEllipseClick;
    private boolean shouldAddBack;
    private Helper.VoidCallback onBack;

    public FolderFaceAdapter(
            List<FileInfo> fileInfoList,
            OnItemClick<FileInfo> onItemClick,
            OnEllipseClick<FileInfo> onEllipseClick,
            boolean shouldAddBack,
            Helper.VoidCallback onBack
    ) {
        this.fileInfoList = fileInfoList;
        this.onItemClick = onItemClick;
        this.onEllipseClick = onEllipseClick;
        this.shouldAddBack = shouldAddBack;
        this.onBack = onBack;

        if (shouldAddBack) {
            this.fileInfoList.add(0, new FileInfo("...", "folder"));
        }
    }

    @NonNull
    @Override
    public FolderFaceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        DirectoryItemBinding binding = DirectoryItemBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new FolderFaceViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull FolderFaceViewHolder holder, int position) {
        FileInfo item = fileInfoList.get(position);

        if (item.getType().equals("folder")) {
            holder.setUpFolder();
        } else holder.setUpFile();
        holder.setName(item.getName());

        if (shouldAddBack && position == 0 && onBack != null) {
            holder.hideEllipse();
            holder.binding.getRoot().setOnClickListener(view -> {
                onBack.invoke();
            });
        } else if (!item.getType().equals("file") && onItemClick != null) {
            holder.binding.getRoot().setOnClickListener(view -> {
                onItemClick.onClick(item);
            });
        }

        if (onEllipseClick != null) {
            holder.binding.ellipse.setOnClickListener(view -> {
                onEllipseClick.onClick(view, item);
            });
        }
    }

    @Override
    public int getItemCount() {
        return fileInfoList.size();
    }

    public interface OnItemClick<T> {
        public void onClick(T item);
    }

    public interface OnEllipseClick<T> {
        public void onClick(View view, T item);
    }

    public class FolderFaceViewHolder extends RecyclerView.ViewHolder {
        private DirectoryItemBinding binding;

        public FolderFaceViewHolder(@NonNull DirectoryItemBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void setUpFile() {
            binding.icon.setImageResource(R.drawable.file);
        }

        public void setUpFolder() {
            binding.icon.setImageResource(R.drawable.folder);
        }

        public void setName(String name) {
            binding.name.setText(name);
        }

        public void hideEllipse() {
            binding.ellipse.setVisibility(View.GONE);
        }
    }
}
