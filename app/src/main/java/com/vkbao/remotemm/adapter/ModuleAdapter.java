package com.vkbao.remotemm.adapter;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.vkbao.remotemm.databinding.ModuleItemBinding;
import com.vkbao.remotemm.model.ModuleModel;

import java.util.List;
import java.util.Map;

public class ModuleAdapter extends RecyclerView.Adapter<ModuleAdapter.ModuleViewHolder>{
    private List<ModuleModel> moduleList;
    private OnClickItem<ModuleModel> listener;

    public ModuleAdapter(List<ModuleModel> moduleList, OnClickItem<ModuleModel> listener) {
        this.moduleList = moduleList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ModuleViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ModuleItemBinding binding = ModuleItemBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new ModuleViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ModuleViewHolder holder, int position) {
        ModuleModel module = moduleList.get(position);
        holder.setModuleName(module.getModule());

        if (listener != null) {
            holder.binding.getRoot().setOnClickListener(view -> {
                listener.setOnClickItem(module);
            });
        }
    }

    @Override
    public int getItemCount() {
        return moduleList.size();
    }

    public void setModuleList(List<ModuleModel> moduleList) {
        this.moduleList = moduleList;
        notifyDataSetChanged();
    }

    public class ModuleViewHolder extends RecyclerView.ViewHolder {
        private ModuleItemBinding binding;

        public ModuleViewHolder(@NonNull ModuleItemBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void setModuleName(String name) {
            binding.moduleName.setText(name);
        }
    }

    public interface OnClickItem<T> {
        public void setOnClickItem(T module);
    }
}
