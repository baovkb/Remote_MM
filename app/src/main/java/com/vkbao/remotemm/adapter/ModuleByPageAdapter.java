package com.vkbao.remotemm.adapter;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.vkbao.remotemm.databinding.ModuleByPageItemBinding;
import com.vkbao.remotemm.model.ModulesByPageModel;

import java.util.List;

public class ModuleByPageAdapter extends RecyclerView.Adapter<ModuleByPageAdapter.ModuleByPageViewHolder> {
    private List<ModulesByPageModel> itemList;
    private OnSwitchChange<ModulesByPageModel> listener;

    public ModuleByPageAdapter(List<ModulesByPageModel> itemList, OnSwitchChange<ModulesByPageModel> listener) {
        this.itemList = itemList;
        this.listener = listener;
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

        if (listener != null) {
            holder.binding.moduleState.setOnCheckedChangeListener((compoundButton, b) -> {
                ModulesByPageModel tmp = new ModulesByPageModel(
                        item.getName(),
                        item.getIdentifier(),
                        !b);
                listener.setOnSwitchChange(tmp);
            });
        }
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

    public interface OnSwitchChange<T> {
        public void setOnSwitchChange(T moduleByPage);
    }
}
