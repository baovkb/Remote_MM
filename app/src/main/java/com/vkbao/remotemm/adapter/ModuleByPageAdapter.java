package com.vkbao.remotemm.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.vkbao.remotemm.databinding.ModuleByPageItemBinding;
import com.vkbao.remotemm.model.ModulesByPageModel;

import java.util.List;

public class ModuleByPageAdapter extends RecyclerView.Adapter<ModuleByPageAdapter.ModuleByPageViewHolder> {
    private List<ModulesByPageModel> itemList;

    public ModuleByPageAdapter(List<ModulesByPageModel> itemList) {
        this.itemList = itemList;
    }

    @NonNull
    @Override
    public ModuleByPageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ModuleByPageItemBinding binding = ModuleByPageItemBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new ModuleByPageViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ModuleByPageViewHolder holder, int position) {
        ModulesByPageModel item = itemList.get(position);
        holder.setModuleName(item.getName());
        holder.setModuleState(!item.isHidden());
    }

    @Override
    public int getItemCount() {
        return itemList.size();
    }

    public class ModuleByPageViewHolder extends RecyclerView.ViewHolder {
        private ModuleByPageItemBinding binding;

        public ModuleByPageViewHolder(@NonNull ModuleByPageItemBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void setModuleName(String name) {
            binding.moduleName.setText(name);
        }

        public void setModuleState(boolean hidden) {
            binding.moduleState.setChecked(hidden);
        }
    }
}
